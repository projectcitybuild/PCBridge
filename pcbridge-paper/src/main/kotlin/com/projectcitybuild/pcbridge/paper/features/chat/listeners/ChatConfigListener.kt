package com.projectcitybuild.pcbridge.paper.features.chat.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatConfigListener(
    private val chatBadgeRepository: ChatBadgeRepository,
    private val chatGroupRepository: ChatGroupRepository,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.chat == next.chat) {
            return
        }

        log.info { "Chat config updated. Rebuilding chat formatting" }

        chatBadgeRepository.invalidateAll()
        chatGroupRepository.invalidateAll()
    }
}
