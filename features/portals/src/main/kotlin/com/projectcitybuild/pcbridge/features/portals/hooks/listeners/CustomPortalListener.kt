package com.projectcitybuild.pcbridge.features.portals.hooks.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.portals.domain.data.MultiversePortalsSettings
import com.projectcitybuild.pcbridge.features.portals.domain.services.CustomPortalService
import kotlinx.coroutines.CancellationException
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class CustomPortalListener(
    private val plugin: Plugin,
    private val service: CustomPortalService,
    private val settings: MultiversePortalsSettings,
) : Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerPortal(event: PlayerPortalEvent) {
        val entry = service.beginPortalEntry(event.player, event.from, event.player.location) ?: return
        if (entry.cancelVanilla) event.isCancelled = true
        activateIfRequested(event.player, entry)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEntityPortal(event: EntityPortalEvent) {
        val portal = service.findAt(event.from) ?: return
        if (!portal.teleportNonPlayers) return
        try {
            event.to = service.entityDestination(portal)
        } catch (failure: Exception) {
            plugin.logger.warning("Could not activate custom portal ${portal.name}: ${failure.message}")
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!settings.useOnMove || event is PlayerTeleportEvent) return
        val destination = event.to ?: return
        if (event.from.blockX == destination.blockX &&
            event.from.blockY == destination.blockY &&
            event.from.blockZ == destination.blockZ &&
            event.from.world === destination.world
        ) {
            return
        }
        val entry = service.beginPortalEntry(event.player, destination) ?: return
        activateIfRequested(event.player, entry)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        service.forgetPlayer(event.player)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!service.isWandEnabled(event.player)) return
        if (event.item?.type != settings.wandMaterial) return
        val first = when (event.action) {
            Action.LEFT_CLICK_BLOCK -> true
            Action.RIGHT_CLICK_BLOCK -> false
            else -> return
        }
        val location = event.clickedBlock?.location ?: return
        if (service.selectCorner(event.player, location, first)) event.isCancelled = true
    }

    private fun activateIfRequested(
        player: Player,
        entry: CustomPortalService.PortalEntry,
    ) {
        if (!entry.shouldActivate) return
        plugin.launch {
            try {
                service.activate(player, entry.portal)
            } catch (failure: CancellationException) {
                throw failure
            } catch (failure: Exception) {
                plugin.logger.severe(
                    "Could not activate custom portal ${entry.portal.name}: ${failure.message}",
                )
                player.sendMessage(l10n.portals.customTeleportFailed(entry.portal.name))
            }
        }
    }
}
