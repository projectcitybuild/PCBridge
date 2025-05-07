package com.projectcitybuild.pcbridge.paper.features.borders.listeners

import com.projectcitybuild.pcbridge.paper.features.borders.actions.PlayerBorderCheck
import com.projectcitybuild.pcbridge.paper.features.borders.repositories.WorldBorderRepository
import org.bukkit.Location
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

class BorderBoundsListener(
    private val worldBorderRepository: WorldBorderRepository,
    private val playerBorderCheck: PlayerBorderCheck,
): Listener {
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerTeleport(event: PlayerTeleportEvent)
        = playerBorderCheck.check(event.player)

    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerPortal(event: PlayerPortalEvent)
        = playerBorderCheck.check(event.player)

    @EventHandler(ignoreCancelled = true)
    suspend fun onCreatureSpawn(event: CreatureSpawnEvent)
        = cancelIfOutsideBorder(event.location, event)

    @EventHandler(ignoreCancelled = true)
    suspend fun onBlockPlace(event: BlockPlaceEvent)
        = cancelIfOutsideBorder(event.block.location, event)

    private suspend fun cancelIfOutsideBorder(location: Location, event: Cancellable) {
        val border = worldBorderRepository.get(location.world)
            ?: return

        if (!border.contains(location)) {
            event.isCancelled = true
        }
    }
}