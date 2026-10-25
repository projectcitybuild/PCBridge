package com.projectcitybuild.pcbridge.features.stats.domain

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.features.stats.domain.repositories.StatsRepository
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

data class CollectedStats(
    val blocksPlaced: Long = 0,
    val blocksDestroyed: Long = 0,
    val afkTime: Long = 0,
)

class StatsCollector(
    private val statsRepository: StatsRepository,
    private val remoteConfig: RemoteConfig,
    private val errorTracker: ErrorTracker,
) {
    private val statsLock = Any()
    private val stats = mutableMapOf<UUID, CollectedStats>()
    private val flushMutex = Mutex()
    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + scopeJob)
    private var job: Job? = null

    fun blockPlaced(playerUuid: UUID) = update(playerUuid) {
        copy(blocksPlaced = blocksPlaced + 1)
    }

    fun blockDestroyed(playerUuid: UUID) = update(playerUuid) {
        copy(blocksDestroyed = blocksDestroyed + 1)
    }

    fun afkEnded(playerUuid: UUID, duration: Duration) = update(playerUuid) {
        copy(afkTime = afkTime + duration.seconds)
    }

    private fun update(
        playerUuid: UUID,
        change: CollectedStats.() -> CollectedStats,
    ) {
        synchronized(statsLock) {
            stats[playerUuid] = (stats[playerUuid] ?: CollectedStats()).change()
        }
    }

    fun start() {
        check(scopeJob.isActive) { "StatsCollector has been shut down" }
        if (job?.isActive == true) return

        logSync.info { "Stats collector started" }
        job = scope.launch {
            while (isActive) {
                val interval = remoteConfig.latest.config.statCollectionIntervalSeconds
                delay(interval.seconds)
                try {
                    flush()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error(e) { "Failed to flush collected stats; the batch will be retried" }
                }
            }
        }
    }

    suspend fun stopCollection() {
        job?.cancelAndJoin()
        job = null
        logSync.info { "Stats collection stopped" }
    }

    suspend fun shutdown() {
        try {
            stopCollection()
        } finally {
            scopeJob.cancel()
        }
    }

    suspend fun stop() {
        stopCollection()
        flush()
        logSync.info { "Stats collector stopped" }
    }

    /**
     * Detached batches are restored on failure. A request may have reached the server
     * before a transport failure, so retries can duplicate delivery without server deduplication.
     */
    suspend fun flush() {
        flushMutex.withLock {
            val batch = synchronized(statsLock) {
                if (stats.isEmpty()) return
                stats.toMap().also { stats.clear() }
            }
            log.trace { "Sending stats ($batch)" }

            try {
                statsRepository.increment(batch.mapKeys { it.key.toString() }.mapValues { it.value.toPlayerStats() })
            } catch (e: Throwable) {
                synchronized(statsLock) {
                    batch.forEach { (playerUuid, detached) ->
                        val current = stats[playerUuid] ?: CollectedStats()
                        stats[playerUuid] = current + detached
                    }
                }
                if (e !is CancellationException) {
                    errorTracker.reportSafely(e)
                }
                throw e
            }
        }
    }
}

private operator fun CollectedStats.plus(other: CollectedStats) = CollectedStats(
    blocksPlaced = blocksPlaced + other.blocksPlaced,
    blocksDestroyed = blocksDestroyed + other.blocksDestroyed,
    afkTime = afkTime + other.afkTime,
)

private fun CollectedStats.toPlayerStats() = PlayerStats(
    blocksPlaced = blocksPlaced,
    blocksDestroyed = blocksDestroyed,
    afkTime = afkTime,
)
