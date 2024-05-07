package com.projectcitybuild.modules.moderation.mute.actions

import com.projectcitybuild.features.joinmessages.PlayerConfig
import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.features.joinmessages.repositories.PlayerConfigRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class MutePlayerTest {
    private lateinit var playerConfigRepository: PlayerConfigRepository

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)
    }

    @Test
    fun `sets the given player's config to muted`() {
        val uuid = UUID.randomUUID()
        val config = PlayerConfig(
            id = 123,
            uuid = uuid,
            isMuted = false,
            isChatBadgeDisabled = false,
            firstSeen = LocalDateTime.now(),
        )
        whenever(playerConfigRepository.get(uuid)).thenReturn(config)

        MutePlayer(playerConfigRepository).execute(uuid, shouldMute = true)

        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(config.copy(isMuted = true), firstValue)
        }
    }

    @Test
    fun `sets the given player's config to unmuted`() {
        val uuid = UUID.randomUUID()
        val config = PlayerConfig(
            id = 123,
            uuid = uuid,
            isMuted = true,
            isChatBadgeDisabled = false,
            firstSeen = LocalDateTime.now(),
        )
        whenever(playerConfigRepository.get(uuid)).thenReturn(config)

        MutePlayer(playerConfigRepository).execute(uuid, shouldMute = false)

        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(config.copy(isMuted = false), firstValue)
        }
    }
}