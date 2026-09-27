package com.projectcitybuild.pcbridge.paper.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.paper.features.stats.domain.StatsCollector
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockChangeListener(
    private val statsCollector: StatsCollector,
): Listener {
    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(
        event: BlockPlaceEvent,
    ) = statsCollector.blockPlaced(playerUuid = event.player.uniqueId)

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(
        event: BlockBreakEvent,
    ) = statsCollector.blockDestroyed(playerUuid = event.player.uniqueId)
}
