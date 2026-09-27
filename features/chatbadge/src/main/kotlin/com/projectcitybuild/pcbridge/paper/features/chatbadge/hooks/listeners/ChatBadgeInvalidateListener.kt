package com.projectcitybuild.pcbridge.paper.features.chatbadge.hooks.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.chatbadge.chatBadgeTracer
import com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.runtime.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.runtime.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.runtime.state.events.PlayerStateUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatBadgeInvalidateListener(
    private val chatBadgeRepository: ChatBadgeRepository,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) =
        event.scopedSync(chatBadgeTracer, this::class.java) {
            val prev = event.prev?.config
            val next = event.next.config

            if (prev?.chat?.badgeIcon == next.chat.badgeIcon) return@scopedSync

            logSync.info { "Chat config updated. Rebuilding chat badges" }

            chatBadgeRepository.invalidateAll()
        }

    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) =
        event.scopedSync(chatBadgeTracer, this::class.java) {
            chatBadgeRepository.invalidate(event.playerUUID)
        }

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) =
        event.scopedSync(chatBadgeTracer, this::class.java) {
            if (event.prevState?.syncedValue?.badges == event.state.syncedValue?.badges) {
                return@scopedSync
            }
            logSync.info { "Invalidating chat badge cache for ${event.playerUUID}" }
            chatBadgeRepository.invalidate(event.playerUUID)
        }
}
