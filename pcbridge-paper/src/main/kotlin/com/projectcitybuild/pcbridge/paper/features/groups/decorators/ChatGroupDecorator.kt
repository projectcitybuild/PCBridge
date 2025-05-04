package com.projectcitybuild.pcbridge.paper.features.groups.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSender
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSenderDecorator
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.ChatGroupRepository
import net.kyori.adventure.text.Component

class ChatGroupDecorator(
    private val chatGroupRepository: ChatGroupRepository,
): ChatSenderDecorator {
    override suspend fun handle(prev: ChatSender): ChatSender {
        val uuid = prev.sender.uniqueId
        val cached = chatGroupRepository.getGroupsComponent(uuid)

        val groups = cached.value
            ?: return prev

        return prev.copy(
            sourceDisplayName = Component.text()
                .append(groups, Component.space(), prev.sourceDisplayName)
                .build(),
        )
    }
}
