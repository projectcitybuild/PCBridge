package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.modules.sessioncache.PendingJoinAction
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.environment.send
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class PendingJoinActionListener(
    private val plugin: Plugin,
    private val sessionCache: SessionCache,
    private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val pendingAction = sessionCache.pendingJoinActions[event.player.uniqueId]

        if (pendingAction == null) {
            logger.debug("No pending action for this player")
            return
        }

        // Delay required to allow time for Player Object to be spawned
        event.player.server.scheduler.scheduleSyncDelayedTask(plugin, {
            when (pendingAction) {
                is PendingJoinAction.TeleportToLocation -> {
                    event.player.teleport(pendingAction.location, PlayerTeleportEvent.TeleportCause.COMMAND)
                }
                is PendingJoinAction.TeleportToPlayer -> {
                    val targetPlayer = plugin.server.getPlayer(pendingAction.targetUUID)
                    if (targetPlayer == null) {
                        event.player.send().error("Could not find target player for teleport")
                        return@scheduleSyncDelayedTask
                    }
                    event.player.teleport(targetPlayer)
                }
            }
        }, 1)

        sessionCache.pendingJoinActions.remove(event.player.uniqueId)
    }
}