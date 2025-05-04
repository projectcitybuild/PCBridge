package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import net.kyori.adventure.text.Component

class ChatGroupMiddleware(
    private val chatGroupRepository: ChatGroupRepository,
): ChatMiddleware {
    override suspend fun handle(chatMessage: ChatMessage): ChatMessage {
        val uuid = chatMessage.source.uniqueId
        val cached = chatGroupRepository.getGroupsComponent(uuid)

        val groups = cached.value
            ?: return chatMessage

        return chatMessage.copy(
            sourceDisplayName = Component.text()
                .append(groups, Component.space(), chatMessage.sourceDisplayName)
                .build(),
        )
    }
}
