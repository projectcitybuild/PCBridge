package com.projectcitybuild.pcbridge.paper.integrations.essentials

import com.earth2me.essentials.Essentials
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.events.PlayerPreTeleportEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotIntegration
import kotlinx.coroutines.runBlocking
import net.ess3.api.events.AfkStatusChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class EssentialsIntegration(
    private val plugin: JavaPlugin,
    private val store: Store,
    private val eventBroadcaster: SpigotEventBroadcaster,
    sentry: SentryReporter,
) : Listener, SpigotIntegration(
        pluginName = "Essentials",
        pluginManager = plugin.server.pluginManager,
        sentry = sentry,
    ) {
    private var essentials: Essentials? = null

    override suspend fun onEnable(loadedPlugin: Plugin) {
        check(loadedPlugin is Essentials) {
            "Found Essentials plugin but cannot cast to Essentials class"
        }
        essentials = loadedPlugin
        plugin.server.pluginManager.registerSuspendingEvents(this, plugin)
        log.info { "Essentials integration enabled" }
    }

    override suspend fun onDisable() {
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
    fun onPlayerPreWarp(event: PlayerPreTeleportEvent) = runCatching {
        if (essentials == null) {
            log.warn { "Essentials integration disabled but it's still listening to events" }
            return@runCatching
        }
        essentials!!
            .getUser(event.player)
            .setLastLocation()

        log.debug { "Registered last location for ${event.player.name} with Essentials" }
    }

    @EventHandler
    fun onPlayerAFKStatusChange(event: AfkStatusChangeEvent) = runCatching {
        if (essentials == null) {
            log.warn { "Essentials integration disabled but it's still listening to events" }
            return@runCatching
        }
        log.info { "Player AFK status changed (${event.value}, ${event.cause})" }

        val playerUuid = event.affected.uuid
        // TODO: clean up this mess...
        runBlocking {
            val players = store.state.players.toMutableMap()
            val prevPlayer = players[playerUuid]
            val nextPlayer = prevPlayer?.copy(afk = event.value)
            if (nextPlayer != null) {
                players[playerUuid] = nextPlayer
            }
            store.mutate {
                it.copy(players = players)
            }
            if (nextPlayer != null) {
                eventBroadcaster.broadcast(
                    PlayerStateUpdatedEvent(
                        prevState = prevPlayer,
                        state = nextPlayer,
                        playerUUID = playerUuid,
                    ),
                )
            }
        }
    }
}
