package com.projectcitybuild.pcbridge.paper.features.chat.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import com.projectcitybuild.pcbridge.paper.features.playerstate.events.PlayerStateUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SyncPlayerChatListener(
    private val chatGroupRepository: ChatGroupRepository,
    private val chatBadgeRepository: ChatBadgeRepository,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        log.info { "Invalidating chat format cache for ${event.playerUUID}" }

        chatGroupRepository.invalidate(event.playerUUID)
        chatBadgeRepository.invalidate(event.playerUUID)
    }
}
