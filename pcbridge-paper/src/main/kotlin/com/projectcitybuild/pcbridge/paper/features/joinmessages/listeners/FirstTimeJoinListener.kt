package com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
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
    fun onPlayerJoin(event: PlayerJoinEvent) {
        logSync.debug { "Checking if first time join" }

        val playerState = store.state.players[event.player.uniqueId]
        if (playerState == null) {
            logSync.warn { "Failed to find state for player: ${event.player.uniqueId}" }
            return
        }
        if (playerState.player?.lastSeenAt != null) {
            logSync.info { "Player last seen ${playerState.player.lastSeenAt}. Not sending first-time join message" }
            return
        }

        logSync.info { "Sending first-time welcome message for ${event.player.name}" }

        if (server.onlinePlayers.isEmpty()) {
            logSync.info { "Skipping. No players online" }
            return
        }

        val config = remoteConfig.latest.config
        val message = MiniMessage.miniMessage().deserialize(
            config.messages.firstTimeJoin,
            Placeholder.component("name", Component.text(event.player.name)),
        )
        server.onlinePlayers
            .filter { it.uniqueId != event.player.uniqueId }
            .forEach { it.sendMessage(message) }
    }
}
