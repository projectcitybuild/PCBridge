package com.projectcitybuild.pcbridge.features.chat.listeners

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class EmojiChatListener: Listener, ChatRenderer.ViewerUnaware {
    @EventHandler(priority = EventPriority.LOW)
    fun onChat(event: AsyncChatEvent) {
        event.renderer(
            ChatRenderer.viewerUnaware(this),
        )
    }

    override fun render(
        source: Player,
        sourceDisplayName: Component,
        message: Component,
    ): Component {
        return message.replaceText { builder ->
            builder.match(pattern).replacement { match, _ ->
                val replaced = emojis[match.group().lowercase()] ?: match.group()
                Component.text(replaced)
            }
        }
    }

    private companion object {
        val emojis: Map<String, String> = mapOf(
            Pair(":skull:", "☠"),
            Pair(":heart:", "❤"),
            Pair(":fire:", "\uD83D\uDD25"),
            Pair(":tm:", "™"),
        )

        val pattern = emojis.keys
            .joinToString(separator = "|")
            .let { pattern -> "(?i)($pattern)" }  // Add case-insensitivity
    }
}
