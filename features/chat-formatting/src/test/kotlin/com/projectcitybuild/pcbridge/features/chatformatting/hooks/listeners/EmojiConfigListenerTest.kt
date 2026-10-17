package com.projectcitybuild.pcbridge.features.chatformatting.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class EmojiConfigListenerTest {
    private val emojiRepository = mock(EmojiRepository::class.java)
    private val listener = EmojiConfigListener(emojiRepository)

    @Test
    fun `updates emoji mapping when remote config changes`() {
        val emojis = mapOf(":wave:" to "👋")

        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(
                prev =
                    RemoteConfigVersion(version = 1, config = RemoteConfigKeyValues()),
                next =
                    RemoteConfigVersion(
                        version = 2,
                        config = RemoteConfigKeyValues(emojis = emojis),
                    ),
            ),
        )

        verify(emojiRepository).set(emojis)
    }

    @Test
    fun `does not update emoji mapping when the mapping is unchanged`() {
        val emojis = mapOf(":wave:" to "👋")

        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(
                prev =
                    RemoteConfigVersion(
                        version = 1,
                        config = RemoteConfigKeyValues(emojis = emojis),
                    ),
                next =
                    RemoteConfigVersion(
                        version = 2,
                        config = RemoteConfigKeyValues(emojis = emojis),
                    ),
            ),
        )

        verify(emojiRepository, never()).set(emojis)
    }

    @Test
    fun `loads emoji mapping when no previous config exists`() {
        val emojis = mapOf(":wave:" to "👋")

        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(
                prev = null,
                next =
                    RemoteConfigVersion(
                        version = 1,
                        config = RemoteConfigKeyValues(emojis = emojis),
                    ),
            ),
        )

        verify(emojiRepository).set(emojis)
    }
}
