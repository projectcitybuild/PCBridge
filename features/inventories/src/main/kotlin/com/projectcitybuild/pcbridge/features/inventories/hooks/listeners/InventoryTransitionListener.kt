package com.projectcitybuild.pcbridge.features.inventories.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.inventories.domain.services.InventoryTransitionService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class InventoryTransitionListener(
    private val plugin: JavaPlugin,
    private val transitionService: InventoryTransitionService,
) : Listener {
    private val pendingJoinReconciliation = ConcurrentHashMap.newKeySet<UUID>()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        if (transitionService.applyPlayerDataOnJoin && event.player.uniqueId in pendingJoinReconciliation) return
        safely("world change", event.player) {
            transitionService.onWorldChanged(event.player, event.from.name, event.player.world.name)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        if (event.isCancelled) return
        val player = event.player
        safely("game-mode change", player) {
            val applyTransition =
                transitionService.onGameModeChanged(player, player.gameMode, event.newGameMode)
                    ?: return@safely
            plugin.server.scheduler.runTask(
                plugin,
                Runnable {
                    if (!player.isOnline) return@Runnable
                    safely("game-mode change", player, applyTransition)
                },
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!transitionService.applyPlayerDataOnJoin) return
        val player = event.player
        val playerId = player.uniqueId
        pendingJoinReconciliation += playerId
        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                if (!pendingJoinReconciliation.remove(playerId) || !player.isOnline) return@Runnable
                safely("join", player) {
                    transitionService.onPlayerJoined(player)
                }
            },
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        pendingJoinReconciliation.remove(event.player.uniqueId)
        safely("quit", event.player) {
            transitionService.onPlayerQuit(event.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val destinationWorld = event.respawnLocation.world?.name ?: return
        safely("respawn", player) {
            transitionService.onPlayerRespawnScheduled(player, destinationWorld)
            plugin.server.scheduler.runTask(
                plugin,
                Runnable {
                    if (!player.isOnline) return@Runnable
                    safely("respawn", player) {
                        transitionService.onPlayerRespawned(player, destinationWorld)
                    }
                },
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        safely("teleport", event.player) {
            transitionService.recordLastLocation(event.player, event.from)
        }
    }

    private inline fun safely(
        operation: String,
        player: Player,
        action: () -> Unit,
    ) {
        try {
            action()
        } catch (failure: Exception) {
            plugin.logger.log(
                Level.SEVERE,
                "Failed to persist or apply Multiverse-Inventories data during $operation for ${player.name}",
                failure,
            )
            player.sendMessage(l10n.inventories.transitionFailed)
        }
    }
}
