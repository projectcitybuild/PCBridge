package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import net.kyori.adventure.text.Component

class ChatBadgeMiddleware(
    private val chatBadgeRepository: ChatBadgeRepository,
): ChatMiddleware {
    override suspend fun handle(chatMessage: ChatMessage): ChatMessage {
        val uuid = chatMessage.source.uniqueId
        val cached = chatBadgeRepository.getComponent(uuid)

        val badge = cached.value
            ?: return chatMessage

        return chatMessage.copy(
            sourceDisplayName = Component.text()
                .append(badge, Component.space(), chatMessage.sourceDisplayName)
                .build(),
        )
    }
}
