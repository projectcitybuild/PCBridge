package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import net.kyori.adventure.text.Component

class ChatEmojiMiddleware : ChatMiddleware {
    override suspend fun handle(chatMessage: ChatMessage): ChatMessage {
        return chatMessage.copy(
            message = chatMessage.message.replaceText { builder ->
                builder.match(pattern).replacement { match, _ ->
                    val replaced = emojis[match.group().lowercase()] ?: match.group()
                    Component.text(replaced)
                }
            }
        )
    }

    private companion object {
        val emojis: Map<String, String> =
            mapOf(
                Pair(":skull:", "☠"),
                Pair(":heart:", "❤"),
                Pair(":fire:", "\uD83D\uDD25"),
                Pair(":tm:", "™"),
            )

        val pattern =
            emojis.keys
                .joinToString(separator = "|")
                .let { pattern -> "(?i)($pattern)" } // Add case-insensitivity
    }
}
