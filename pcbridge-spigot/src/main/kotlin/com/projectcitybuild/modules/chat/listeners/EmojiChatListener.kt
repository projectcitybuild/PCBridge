package com.projectcitybuild.modules.chat.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent

class EmojiChatListener: SpigotListener<AsyncPlayerChatEvent> {

    companion object {
        val emojis: Map<String, String> = mapOf(
            Pair(":skull:", "☠"),
            Pair(":heart:", "❤"),
            Pair(":fire:", "\uD83D\uDD25"),
            Pair(":tm:", "™"),
        )

        val regex = emojis.keys
            .joinToString(separator = "|")
            .let { pattern -> "(?i)($pattern)" }  // Add case-insensitivity
            .toRegex()
    }

    @EventHandler
    override suspend fun handle(event: AsyncPlayerChatEvent) {
        event.message = event.message.replace(
            regex = regex,
            transform = { match -> emojis[match.value.lowercase()] ?: match.value },
        )
    }
}
