package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.modules.sessioncache.SessionCache
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class PendingWarpConnectListener(
    private val plugin: Plugin,
    private val sessionCache: SessionCache,
    private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val pendingWarpLocation = sessionCache.pendingWarps[event.player.uniqueId]

        if (pendingWarpLocation == null) {
            logger.debug("No pending warps for this player")
            return
        }

        // Delay required to allow time for Player Object to be spawned
        event.player.server.scheduler.scheduleSyncDelayedTask(plugin, {
            event.player.teleport(pendingWarpLocation, PlayerTeleportEvent.TeleportCause.COMMAND)
            sessionCache.pendingWarps.remove(event.player.uniqueId)
        }, 3)
    }
}