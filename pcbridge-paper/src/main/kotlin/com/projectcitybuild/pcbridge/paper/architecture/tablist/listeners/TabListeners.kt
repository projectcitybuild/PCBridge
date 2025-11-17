package com.projectcitybuild.pcbridge.paper.architecture.tablist.listeners

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class TabListeners(
    private val server: Server,
    private val tabRenderer: TabRenderer,
): Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        log.debug { "PlayerJoinEvent: setting tab for joining player" }

        // Render the tab for the player as soon as they join so that
        // they don't have the default vanilla one
        tabRenderer.updatePlayerName(event.player)
        tabRenderer.updateHeaderAndFooter(event.player)
    }

    @EventHandler
    suspend fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.tab != next.tab) {
            log.debug { "RemoteConfigUpdatedEvent: updating all tabs" }

            server.onlinePlayers.forEach { player ->
                tabRenderer.updatePlayerName(player)
                tabRenderer.updateHeaderAndFooter(player)
            }
        }
    }
}