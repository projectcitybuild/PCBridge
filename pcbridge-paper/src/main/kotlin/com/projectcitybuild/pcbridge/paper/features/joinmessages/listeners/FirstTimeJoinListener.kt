package com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.paper.core.logger.log
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.store.Store
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class FirstTimeJoinListener(
    private val server: Server,
    private val store: Store,
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerStateUpdated(event: PlayerJoinEvent) {
        log.debug { "checking if first time join" }

        val playerState = store.state.players[event.player.uniqueId]
        if (playerState == null) {
            log.warn { "Failed to find state for player: ${event.player.uniqueId}" }
            return
        }
        if (playerState.player == null) {
            log.warn { "No player data found for ${event.player.uniqueId}" }
            return
        }
        if (playerState.player.lastSeenAt != null) {
            log.debug { "Player last seen ${playerState.player.lastSeenAt}. Not sending first-time join message" }
            return
        }

        log.info { "Sending first-time welcome message for ${event.player.name}" }

        if (server.onlinePlayers.isEmpty()) {
            log.debug { "Skipping. No players online..." }
            return
        }

        val config = remoteConfig.latest.config
        val message =
            MiniMessage.miniMessage().deserialize(
                config.messages.firstTimeJoin,
                Placeholder.component("name", Component.text(event.player.name)),
            )
        server.onlinePlayers
            .filter { it.uniqueId != event.player.uniqueId }
            .forEach { it.sendMessage(message) }
    }
}
