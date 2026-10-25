package com.projectcitybuild.pcbridge.features.stats.domain

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.features.stats.domain.repositories.StatsRepository
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import java.time.Duration
import java.util.UUID

class StatsCollectorTest {
    @Test
    fun `aggregates player activity and clears it after a successful flush`() = runTest {
        val repository = mock(StatsRepository::class.java)
        val collector = collector(repository)
        val playerUuid = UUID.randomUUID()
        val expectedStats =
            mapOf(
                playerUuid.toString() to
                    PlayerStats(
                        blocksPlaced = 2,
                        blocksDestroyed = 1,
                        afkTime = 65,
                    ),
            )

        collector.blockPlaced(playerUuid)
        collector.blockPlaced(playerUuid)
        collector.blockDestroyed(playerUuid)
        collector.afkEnded(playerUuid, Duration.ofSeconds(65))
        collector.flush()
        collector.flush()

        verify(repository, times(1)).increment(expectedStats)
    }

    @Test
    fun `does not send an empty batch`() = runTest {
        val repository = mock(StatsRepository::class.java)
        val collector = collector(repository)

        collector.flush()

        verify(repository, never()).increment(any())
    }

    @Test
    fun `reports failed flushes and keeps stats available for retry`() = runTest {
        val repository = mock(StatsRepository::class.java)
        val errorTracker = mock(ErrorTracker::class.java)
        val collector = collector(repository, errorTracker)
        val playerUuid = UUID.randomUUID()
        val stats = mapOf(
            playerUuid.toString() to
                PlayerStats(
                    blocksPlaced = 1,
                    blocksDestroyed = 0,
                    afkTime = 0,
                ),
        )
        val failure = IllegalStateException("request failed")
        whenever(repository.increment(any())).thenThrow(failure)

        collector.blockPlaced(playerUuid)
        repeat(2) {
            val thrown = runCatching { collector.flush() }.exceptionOrNull()
            assertSame(failure, thrown)
        }

        verify(repository, times(2)).increment(stats)
        verify(errorTracker, times(2)).reportSafely(failure)
    }

    @Test
    fun `failed detached batch merges with concurrent increments`() = runTest {
        val repository = mock(StatsRepository::class.java)
        val collector = collector(repository)
        val playerUuid = UUID.randomUUID()
        val requestStarted = CompletableDeferred<Unit>()
        val allowFailure = CompletableDeferred<Unit>()
        val failure = IllegalStateException("request failed")

        doAnswer { invocation ->
            runBlocking {
                requestStarted.complete(Unit)
                allowFailure.await()
            }
            throw failure
        }.whenever(repository).increment(any())

        collector.blockPlaced(playerUuid)
        val firstFlush = async(Dispatchers.IO) {
            runCatching { collector.flush() }.exceptionOrNull()
        }
        requestStarted.await()
        collector.blockPlaced(playerUuid)
        allowFailure.complete(Unit)
        assertSame(failure, firstFlush.await())

        doAnswer { null }.whenever(repository).increment(any())
        collector.flush()

        verify(repository).increment(
            mapOf(
                playerUuid.toString() to PlayerStats(
                    blocksPlaced = 1,
                    blocksDestroyed = 0,
                    afkTime = 0,
                ),
            ),
        )
        verify(repository).increment(
            mapOf(
                playerUuid.toString() to PlayerStats(
                    blocksPlaced = 2,
                    blocksDestroyed = 0,
                    afkTime = 0,
                ),
            ),
        )
    }

    private fun collector(
        repository: StatsRepository,
        errorTracker: ErrorTracker = mock(ErrorTracker::class.java),
    ) = StatsCollector(
        statsRepository = repository,
        remoteConfig = mock(RemoteConfig::class.java),
        errorTracker = errorTracker,
    )
}
