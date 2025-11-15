package com.projectcitybuild.pcbridge.paper.features.chatbadge.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.chatbadge.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatBadgeInvalidateListener(
    private val chatBadgeRepository: ChatBadgeRepository,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.chat?.badgeIcon == next.chat.badgeIcon) return

        log.info { "Chat config updated. Rebuilding chat badges" }

        chatBadgeRepository.invalidateAll()
    }

    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent)
        = chatBadgeRepository.invalidate(event.playerUUID)

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.badges == event.state.badges) return

        log.info { "Invalidating chat badge cache for ${event.playerUUID}" }
        chatBadgeRepository.invalidate(event.playerUUID)
    }
}
