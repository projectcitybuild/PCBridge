package com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessageDecorator
import com.projectcitybuild.pcbridge.paper.features.chatformatting.domain.repositories.EmojiRepository
import net.kyori.adventure.text.Component

class ChatEmojiDecorator(
    private val emojiRepository: EmojiRepository,
): ChatMessageDecorator {
    override suspend fun decorate(prev: ChatMessage): ChatMessage {
        val emojiPattern = emojiRepository.emojiPattern

        return prev.copy(
            message = prev.message.replaceText { builder ->
                builder.match(emojiPattern).replacement { match, _ ->
                    val replaced = emojiRepository.emoji(match.group().lowercase()) ?: match.group()
                    Component.text(replaced)
                }
            }
        )
    }
}
