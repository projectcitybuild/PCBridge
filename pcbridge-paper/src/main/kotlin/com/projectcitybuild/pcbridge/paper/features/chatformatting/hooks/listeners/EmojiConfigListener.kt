package com.projectcitybuild.pcbridge.paper.features.chatformatting.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.chatformatting.chatFormattingTracer
import com.projectcitybuild.pcbridge.paper.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.paper.features.config.domain.data.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EmojiConfigListener(
    private val emojiRepository: EmojiRepository,
): Listener {
    @EventHandler
    fun onRemoteConfigUpdated(
        event: RemoteConfigUpdatedEvent,
    ) = event.scopedSync(chatFormattingTracer, this::class.java) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.emojis != next.emojis) {
            logSync.debug { "RemoteConfigUpdatedEvent: updating emoji mapping" }
            emojiRepository.set(next.emojis)
        }
    }
}