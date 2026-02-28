package com.projectcitybuild.pcbridge.paper.features.roles.hooks.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSender
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSenderDecorator
import com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories.ChatRoleRepository
import net.kyori.adventure.text.Component

class ChatRoleDecorator(
    private val chatRoleRepository: ChatRoleRepository,
): ChatSenderDecorator {
    override suspend fun decorate(prev: ChatSender): ChatSender {
        val uuid = prev.sender.uniqueId
        val cached = chatRoleRepository.getRolesComponent(uuid)

        val roles = cached.value
            ?: return prev

        return prev.copy(
            sourceDisplayName = Component.text()
                .append(roles, Component.space(), prev.sourceDisplayName)
                .build(),
        )
    }
}
