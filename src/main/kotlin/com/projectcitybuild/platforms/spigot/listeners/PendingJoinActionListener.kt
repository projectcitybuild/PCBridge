package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.modules.sessioncache.PendingJoinAction
import com.projectcitybuild.modules.sessioncache.SessionCache
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
            }
        }, 3)

        sessionCache.pendingJoinActions.remove(event.player.uniqueId)
    }
}