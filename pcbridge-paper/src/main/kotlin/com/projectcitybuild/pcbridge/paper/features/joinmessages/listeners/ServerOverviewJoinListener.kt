package com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val config = remoteConfig.latest.config
        val message = config.messages.welcome

        event.player.sendMessage(
            MiniMessage.miniMessage().deserialize(message),
        )
    }
}
