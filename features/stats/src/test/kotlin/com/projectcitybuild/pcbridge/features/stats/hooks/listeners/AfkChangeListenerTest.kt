package com.projectcitybuild.pcbridge.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSession
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateDestroyedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class AfkChangeListenerTest {
    private val statsCollector = mock(StatsCollector::class.java)
    private val localizedTime = mock(LocalizedTime::class.java)
    private val listener = AfkChangeListener(statsCollector, localizedTime)

    @Test
    fun `records elapsed afk time when a player leaves afk`() {
        val playerUuid = UUID.randomUUID()
        val startedAt = Instant.parse("2024-01-01T00:00:00Z")
        whenever(localizedTime.nowInstant()).thenReturn(startedAt.plusSeconds(75))

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(afkStartedAt = startedAt),
                state = playerSession(),
                playerUUID = playerUuid,
            ),
        )

        verify(statsCollector).afkEnded(playerUuid, Duration.ofSeconds(75))
    }

    @Test
    fun `does not record afk time when the player remains afk`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(afkStartedAt = Instant.EPOCH),
                state = playerSession(afkStartedAt = Instant.EPOCH),
                playerUUID = playerUuid,
            ),
        )

        verify(statsCollector, never()).afkEnded(any(), any())
    }

    @Test
    fun `does not record afk time when the previous state was not afk`() {
        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(),
                state = playerSession(),
                playerUUID = UUID.randomUUID(),
            ),
        )

        verify(statsCollector, never()).afkEnded(any(), any())
    }

    @Test
    fun `records afk time when an afk player state is destroyed`() {
        val playerUuid = UUID.randomUUID()
        val startedAt = Instant.parse("2024-01-01T00:00:00Z")
        whenever(localizedTime.nowInstant()).thenReturn(startedAt.plusSeconds(20))

        listener.onPlayerStateDestroyed(
            PlayerStateDestroyedEvent(
                playerData = playerSession(afkStartedAt = startedAt),
                playerUUID = playerUuid,
            ),
        )

        verify(statsCollector).afkEnded(playerUuid, Duration.ofSeconds(20))
    }

    @Test
    fun `does not record afk time when a non-afk player state is destroyed`() {
        listener.onPlayerStateDestroyed(
            PlayerStateDestroyedEvent(
                playerData = playerSession(),
                playerUUID = UUID.randomUUID(),
            ),
        )

        verify(statsCollector, never()).afkEnded(any(), any())
    }

    private fun playerSession(afkStartedAt: Instant? = null) =
        PlayerSession(
            synced = PlayerSyncedState.Unavailable,
            connectedAt = LocalDateTime.MIN,
            afkStartedAt = afkStartedAt,
        )
}
