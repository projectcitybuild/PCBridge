package com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import com.projectcitybuild.pcbridge.paper.features.stats.statsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockChangeListener(
    private val statsCollector: StatsCollector,
): Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onBlockPlace(
        event: BlockPlaceEvent,
    ) = event.scoped(statsTracer, this::class.java) {
        statsCollector.blockPlaced(playerUuid = event.player.uniqueId)
    }

    @EventHandler(ignoreCancelled = true)
    suspend fun onBlockBreak(
        event: BlockBreakEvent,
    ) = event.scoped(statsTracer, this::class.java) {
        statsCollector.blockDestroyed(playerUuid = event.player.uniqueId)
    }
}
