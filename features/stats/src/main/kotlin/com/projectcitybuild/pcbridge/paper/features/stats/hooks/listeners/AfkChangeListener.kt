package com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.paper.runtime.state.events.PlayerStateDestroyedEvent
import com.projectcitybuild.pcbridge.paper.runtime.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration
import java.time.Instant
import java.util.UUID

class AfkChangeListener(
    private val statsCollector: StatsCollector,
    private val localizedTime: LocalizedTime,
): Listener {
    @EventHandler(ignoreCancelled = true)
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        val prevState = event.prevState
        if (prevState?.afkStartedAt != null && !event.state.afk) {
            collect(event.playerUUID, prevState.afkStartedAt)
        }
    }

    fun onPlayerStateDestroyed(event: PlayerStateDestroyedEvent) {
        val playerData = event.playerData
        if (playerData?.afkStartedAt != null) {
            collect(event.playerUUID, playerData.afkStartedAt)
        }
    }

    private fun collect(playerUuid: UUID, afkStartedAt: Instant?) {
        val now = localizedTime.nowInstant()
        val duration = Duration.between(afkStartedAt, now)

        statsCollector.afkEnded(
            playerUuid = playerUuid,
            duration = duration,
        )
        logSync.debug { "Collected AFK duration of ${duration.seconds} seconds" }
    }
}
