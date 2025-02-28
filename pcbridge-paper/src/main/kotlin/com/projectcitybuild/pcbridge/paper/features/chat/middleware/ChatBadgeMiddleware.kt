package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.Chat
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import net.kyori.adventure.text.Component

class ChatBadgeMiddleware(
    private val chatBadgeRepository: ChatBadgeRepository,
): ChatMiddleware {
    override suspend fun handle(chat: Chat): Chat {
        val uuid = chat.source.uniqueId
        val cached = chatBadgeRepository.getComponent(uuid)

        val badge = cached.value
            ?: return chat

        return chat.copy(
            sourceDisplayName = Component.text()
                .append(badge, Component.space(), chat.sourceDisplayName)
                .build(),
        )
    }
}
