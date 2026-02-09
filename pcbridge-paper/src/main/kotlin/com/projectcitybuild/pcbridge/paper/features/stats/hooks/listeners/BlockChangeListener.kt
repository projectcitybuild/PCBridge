package com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsAccumulator
import com.projectcitybuild.pcbridge.paper.features.stats.statsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockChangeListener(
    private val statsAccumulator: StatsAccumulator,
): Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onBlockPlace(
        event: BlockPlaceEvent,
    ) = event.scoped(statsTracer, this::class.java) {
        statsAccumulator.blockPlaced(uuid = event.player.uniqueId)
    }

    @EventHandler(ignoreCancelled = true)
    suspend fun onBlockBreak(
        event: BlockBreakEvent,
    ) = event.scoped(statsTracer, this::class.java) {
        statsAccumulator.blockDestroyed(uuid = event.player.uniqueId)
    }
}
