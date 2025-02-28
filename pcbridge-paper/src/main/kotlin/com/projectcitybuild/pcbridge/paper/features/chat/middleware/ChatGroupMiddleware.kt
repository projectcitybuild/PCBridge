package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.Chat
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import net.kyori.adventure.text.Component

class ChatGroupMiddleware(
    private val chatGroupRepository: ChatGroupRepository,
): ChatMiddleware {
    override suspend fun handle(chat: Chat): Chat {
        val uuid = chat.source.uniqueId
        val cached = chatGroupRepository.getGroupsComponent(uuid)

        val groups = cached.value
            ?: return chat

        return chat.copy(
            sourceDisplayName = Component.text()
                .append(groups, Component.space(), chat.sourceDisplayName)
                .build(),
        )
    }
}
