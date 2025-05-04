package com.projectcitybuild.pcbridge.paper.features.badge.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.badge.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class BadgeInvalidateListener(
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
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        log.info { "Invalidating chat badge cache for ${event.playerUUID}" }

        chatBadgeRepository.invalidate(event.playerUUID)
    }
}
