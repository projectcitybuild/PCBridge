package com.projectcitybuild.pcbridge.paper.features.chatemojis.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import com.projectcitybuild.pcbridge.paper.features.chatemojis.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EmojiConfigListener(
    private val emojiRepository: EmojiRepository,
): Listener {
    @EventHandler
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.emojis != next.emojis) {
            deprecatedLog.debug { "RemoteConfigUpdatedEvent: updating emoji mapping" }
            emojiRepository.set(next.emojis)
        }
    }
}