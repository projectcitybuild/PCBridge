package com.projectcitybuild.pcbridge.paper.features.chatemojis.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessageDecorator
import net.kyori.adventure.text.Component

class ChatEmojiDecorator: ChatMessageDecorator {
    override suspend fun decorate(prev: ChatMessage): ChatMessage {
        return prev.copy(
            message = prev.message.replaceText { builder ->
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
