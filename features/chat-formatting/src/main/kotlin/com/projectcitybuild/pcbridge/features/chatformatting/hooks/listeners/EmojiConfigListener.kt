package com.projectcitybuild.pcbridge.features.chatformatting.hooks.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.chatformatting.chatFormattingTracer
import com.projectcitybuild.pcbridge.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.runtime.listeners.scopedSync
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EmojiConfigListener(
    private val emojiRepository: EmojiRepository,
) : Listener {
    @EventHandler
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) =
        event.scopedSync(chatFormattingTracer, this::class.java) {
            val prev = event.prev?.config
            val next = event.next.config

            if (prev?.emojis != next.emojis) {
                logSync.debug { "RemoteConfigUpdatedEvent: updating emoji mapping" }
                emojiRepository.set(next.emojis)
            }
        }
}
