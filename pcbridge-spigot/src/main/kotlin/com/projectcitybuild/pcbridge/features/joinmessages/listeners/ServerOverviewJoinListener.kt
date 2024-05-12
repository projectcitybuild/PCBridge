package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.core.config.Config
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val config: Config,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val message = config.get().messages.welcome

        event.player.sendMessage(
            MiniMessage.miniMessage().deserialize(message),
        )
    }
}
