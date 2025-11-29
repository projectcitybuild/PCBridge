package com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSender
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSenderDecorator
import com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.repositories.ChatBadgeRepository
import net.kyori.adventure.text.Component

class ChatBadgeDecorator(
    private val chatBadgeRepository: ChatBadgeRepository,
): ChatSenderDecorator {
    override suspend fun decorate(prev: ChatSender): ChatSender {
        val uuid = prev.sender.uniqueId
        val cached = chatBadgeRepository.getComponent(uuid)

        val badge = cached.value
            ?: return prev

        return prev.copy(
            sourceDisplayName = Component.text()
                .append(badge, Component.space(), prev.sourceDisplayName)
                .build(),
        )
    }
}
