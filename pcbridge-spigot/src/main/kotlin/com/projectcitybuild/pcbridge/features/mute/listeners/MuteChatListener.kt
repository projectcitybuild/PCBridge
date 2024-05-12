package com.projectcitybuild.pcbridge.features.mute.listeners

import io.github.reactivecircus.cache4k.Cache
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.UUID

class MuteChatListener(
    private val mutedPlayers: Cache<UUID, Unit>,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncChatEvent) {
        if (mutedPlayers.get(event.player.uniqueId) != null) {
            event.isCancelled = true
            event.player.sendMessage(
                Component.text("You cannot talk while muted")
                    .color(NamedTextColor.RED),
            )
        }
    }
}
