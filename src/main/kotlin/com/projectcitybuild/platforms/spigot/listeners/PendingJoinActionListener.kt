package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.sessioncache.SessionCache
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class PendingJoinActionListener(
    private val sessionCache: SessionCache,
    private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val pendingAction = sessionCache.pendingJoinActions[event.player.uniqueId]

        if (pendingAction == null) {
            logger.debug("No pending action for this player")
        } else {
            pendingAction(event.player, event)
            sessionCache.pendingJoinActions.remove(event.player.uniqueId)
        }
    }
}