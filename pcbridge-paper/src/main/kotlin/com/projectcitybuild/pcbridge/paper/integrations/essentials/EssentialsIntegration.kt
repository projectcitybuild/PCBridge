package com.projectcitybuild.pcbridge.paper.integrations.essentials

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.events.PlayerPreTeleportEvent
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.players
import kotlinx.coroutines.runBlocking
import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.NickChangeEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val session: SessionStore,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val tabRenderer: TabRenderer,
    private val errorTracker: ErrorTracker,
) : Listener {
    private var essentials: Essentials? = null
    private val tracer = TracerFactory.make("integrations.essentials")

    suspend fun enable() = runCatching {
        val integratedPlugin = server.pluginManager.getPlugin("Essentials")
        if (integratedPlugin == null) {
            log.warn { "Cannot find Essentials. Disabling integration" }
            return@runCatching
        }
        check(integratedPlugin is Essentials) {
            "Found Essentials plugin but cannot cast to Essentials class"
        }
        essentials = integratedPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        log.info { "Essentials integration enabled" }
    }.onFailure {
        log.error(it) { "Failed to enable Essentials integration" }
        errorTracker.report(it)
    }

    fun disable() {
        if (essentials != null) {
            PlayerPreTeleportEvent.getHandlerList().unregister(this)
            essentials = null
        }
    }

    /**
     * Sets the player's current position as their last known location
     * in Essentials
     */
    @EventHandler
    fun onPlayerPreTeleport(
        event: PlayerPreTeleportEvent,
    ) = event.scopedSync(tracer, this::class.java) {
        if (essentials == null) {
            logSync.warn { "Essentials integration disabled but it's still listening to events" }
            return@scopedSync
        }
        essentials!!
            .getUser(event.player)
            .setLastLocation()

        logSync.debug { "Registered last location for ${event.player.name} with Essentials" }
    }

    @EventHandler
    fun onPlayerAFKStatusChange(
        event: AfkStatusChangeEvent,
    ) = event.scopedSync(tracer, this::class.java) {
        if (essentials == null) {
            logSync.warn { "Essentials integration disabled but it's still listening to events" }
            return@scopedSync
        }
        logSync.info { "Player AFK status changed (${event.value}, ${event.cause})" }

        val playerUuid = event.affected.uuid
        runBlocking {
            val playerSession = session.state.players[playerUuid]
            val updated = playerSession?.copy(afk = event.value)
            if (updated != null) {
                session.mutate { state ->
                    state.copy(players = state.players + mapOf(playerUuid to updated))
                }
                eventBroadcaster.broadcast(
                    PlayerStateUpdatedEvent(
                        prevState = playerSession,
                        state = updated,
                        playerUUID = playerUuid,
                    ),
                )
            }
        }
    }

    @EventHandler
    suspend fun onPlayerNicknameChange(
        event: NickChangeEvent,
    ) = event.scoped(tracer, this::class.java) {
        val playerUuid = event.affected.uuid
        log.info { "Player nickname changed (${event.value}, $playerUuid)" }

        server.getPlayer(playerUuid)?.let { player ->
            tabRenderer.updatePlayerName(player)
        }
    }
}
