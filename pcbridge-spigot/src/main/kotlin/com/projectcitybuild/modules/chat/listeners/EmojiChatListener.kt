package com.projectcitybuild.modules.chat.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent

class EmojiChatListener: SpigotListener<AsyncPlayerChatEvent> {

    @EventHandler
    override suspend fun handle(event: AsyncPlayerChatEvent) {
        event.message = event.message
            .replace(oldValue = ":skull:", newValue = "☠", ignoreCase = true)
    }
}