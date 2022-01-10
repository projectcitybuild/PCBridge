package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.old_modules.sessioncache.PendingJoinAction
import com.projectcitybuild.old_modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.environment.send
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class PendingJoinActionListener(
    private val plugin: Plugin,
    private val sessionCache: SessionCache,
    private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val pendingAction = sessionCache.pendingJoinActions[event.player.uniqueId]

        if (pendingAction == null) {
            logger.debug("No pending action for this player")
            return
        }

        when (pendingAction) {
            is PendingJoinAction.TeleportToLocation -> {
                event.spawnLocation = pendingAction.location
            }
            is PendingJoinAction.TeleportToPlayer -> {
                val targetPlayer = plugin.server.getPlayer(pendingAction.targetUUID)
                if (targetPlayer == null) {
                    event.player.send().error("Could not find target player for teleport")
                    return
                }
                event.spawnLocation = targetPlayer.location
            }
        }

        sessionCache.pendingJoinActions.remove(event.player.uniqueId)
    }
}