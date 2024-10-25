package com.projectcitybuild.pcbridge.features.chat.listeners

import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.core.remoteconfig.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.features.chat.repositories.ChatGroupRepository
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
