package com.projectcitybuild.pcbridge.paper.features.groups.listener

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.ChatGroupRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatGroupInvalidateListener(
    private val chatGroupRepository: ChatGroupRepository,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        chatGroupRepository.invalidate(event.playerUUID)
    }

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.groups == event.state.groups) return

        log.info { "Invalidating chat group cache for ${event.playerUUID}" }
        chatGroupRepository.invalidate(event.playerUUID)
    }
}
