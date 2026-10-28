package com.projectcitybuild.pcbridge.features.chatbadge.hooks.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.chatbadge.chatBadgeTracer
import com.projectcitybuild.pcbridge.features.chatbadge.domain.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatBadgeInvalidateListener(
    private val chatBadgeRepository: ChatBadgeRepository,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) =
        event.tracedSync(chatBadgeTracer, this::class.java) {
            val prev = event.prev?.config
            val next = event.next.config

            if (prev?.chat?.badgeIcon == next.chat.badgeIcon) return@tracedSync

            logSync.info { "Chat config updated. Rebuilding chat badges" }

            chatBadgeRepository.invalidateAll()
        }

    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) =
        event.tracedSync(chatBadgeTracer, this::class.java) {
            chatBadgeRepository.invalidate(event.playerUUID)
        }

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) =
        event.tracedSync(chatBadgeTracer, this::class.java) {
            if (event.prevState?.syncedValue?.badges == event.state.syncedValue?.badges) {
                return@tracedSync
            }
            logSync.info { "Invalidating chat badge cache for ${event.playerUUID}" }
            chatBadgeRepository.invalidate(event.playerUUID)
        }
}
