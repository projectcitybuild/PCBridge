package com.projectcitybuild.listeners

import com.projectcitybuild.core.SpigotListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

class EmojiChatListener: SpigotListener {

    @EventHandler(priority = EventPriority.NORMAL)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        event.message = event.message
            .replace(oldValue = ":skull:", newValue = "☠", ignoreCase = true)
    }
}
