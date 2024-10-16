package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.core.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val config: Config,
) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val joinMessage = config.get().messages.join

        event.joinMessage(
            MiniMessage.miniMessage().deserialize(
                joinMessage,
                Placeholder.component("name", Component.text(event.player.name)),
            ),
        )
    }
}
