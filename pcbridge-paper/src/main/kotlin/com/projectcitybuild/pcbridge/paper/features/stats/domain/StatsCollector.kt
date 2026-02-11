package com.projectcitybuild.pcbridge.paper.features.stats.domain

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.stats.domain.repositories.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class CollectedStats(
    val blocksPlaced: Long = 0,
    val blocksDestroyed: Long = 0,
    val blocksTravelled: Double = 0.0,
)

class StatsCollector(
    private val statsRepository: StatsRepository,
) {
    private val stats = ConcurrentHashMap<UUID, CollectedStats>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun blockPlaced(playerUuid: UUID) {
        val current = stats[playerUuid] ?: CollectedStats()

        stats[playerUuid] = current.copy(
            blocksPlaced = current.blocksPlaced + 1
        )
    }

    fun blockDestroyed(playerUuid: UUID) {
        val current = stats[playerUuid] ?: CollectedStats()

        stats[playerUuid] = current.copy(
            blocksDestroyed = current.blocksDestroyed + 1
        )
    }

    fun start() {
        logSync.info { "Stats collector started" }

        job = scope.launch {
            val running = job?.isActive ?: false
            while (running) {
                delay(15.seconds)
                sendStats()
            }
        }
    }

    suspend fun stop() {
        job?.cancel()
        job = null

        if (stats.isNotEmpty()) {
            logSync.info { "Sending stats before stopping" }
            sendStats()
        }
        logSync.info { "Stats collector stopped" }
    }

    private suspend fun sendStats() {
        if (stats.isEmpty()) return

        log.trace { "Sending stats (${stats})" }

        statsRepository.increment(
            stats
                .map { it.key.toString() to it.value.toPlayerStats() }
                .toMap()
        )
        stats.clear()
    }
}

private fun CollectedStats.toPlayerStats() = PlayerStats(
    blocksPlaced = blocksPlaced,
    blocksDestroyed = blocksDestroyed,
)