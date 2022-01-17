package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import javax.inject.Inject

class PendingJoinActionListener @Inject constructor(
    private val spigotSessionCache: SpigotSessionCache,
    private val logger: PlatformLogger
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val pendingAction = spigotSessionCache.pendingJoinActions[event.player.uniqueId]

        if (pendingAction == null) {
            logger.debug("No pending action for this player")
        } else {
            pendingAction(event.player, event)
            spigotSessionCache.pendingJoinActions.remove(event.player.uniqueId)
        }
    }
}