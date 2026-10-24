package com.projectcitybuild.pcbridge.integrations.essentials

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.core.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.runtime.listeners.scoped
import com.projectcitybuild.pcbridge.runtime.listeners.scopedSync
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
import com.projectcitybuild.pcbridge.runtime.tablist.TabRenderer
import com.projectcitybuild.pcbridge.runtime.teleportation.events.PlayerPreTeleportEvent
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.players
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import net.ess3.api.events.AfkStatusChangeEvent
import net.ess3.api.events.NickChangeEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val session: SessionStore,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val tabRenderer: TabRenderer,
    private val localizedTime: LocalizedTime,
) : PaperIntegration,
    Listener {
    override val priority = 200

    private var essentials: Essentials? = null
    private val tracer = TracerFactory.make("integrations.essentials")

    override suspend fun enable() {
        try {
            val integratedPlugin = server.pluginManager.getPlugin("Essentials")
            if (integratedPlugin == null) {
                log.warn { "Cannot find Essentials. Disabling integration" }
                return
            }
            check(integratedPlugin is Essentials) {
                "Found Essentials plugin but cannot cast to Essentials class"
            }
            essentials = integratedPlugin
            plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
            log.info { "Essentials integration enabled" }
        } catch (failure: Throwable) {
            try {
                disable()
            } catch (cleanupFailure: Throwable) {
                failure.addSuppressed(cleanupFailure)
            }
            if (failure !is CancellationException) {
                log.error(failure) { "Failed to enable Essentials integration" }
            }
            throw failure
        }
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
        essentials = null
    }

    /**
     * Sets the player's current position as their last known location
     * in Essentials
     */
    @EventHandler
    fun onPlayerPreTeleport(event: PlayerPreTeleportEvent) =
        event.scopedSync(tracer, this::class.java) {
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
    fun onPlayerAFKStatusChange(event: AfkStatusChangeEvent) =
        event.scopedSync(tracer, this::class.java) {
            if (essentials == null) {
                logSync.warn { "Essentials integration disabled but it's still listening to events" }
                return@scopedSync
            }
            logSync.info { "Player AFK status changed (${event.value}, ${event.cause})" }

            val playerUuid = event.affected.uuid
            runBlocking {
                val playerSession = session.state.players[playerUuid]
                val updated =
                    playerSession?.copy(
                        afkStartedAt = if (event.value) localizedTime.nowInstant() else null,
                    )
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
    suspend fun onPlayerNicknameChange(event: NickChangeEvent) =
        event.scoped(tracer, this::class.java) {
            val playerUuid = event.affected.uuid
            log.info { "Player nickname changed (${event.value}, $playerUuid)" }

            server.getPlayer(playerUuid)?.let { player ->
                tabRenderer.updatePlayerName(player)
            }
        }
}
