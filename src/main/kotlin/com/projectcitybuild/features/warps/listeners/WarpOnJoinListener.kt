package com.projectcitybuild.features.warps.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import javax.inject.Inject

class WarpOnJoinListener @Inject constructor(
    private val queuedWarpRepository: QueuedWarpRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
): SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val playerUUID = event.player.uniqueId
        val serverName = config.get(PluginConfig.SPIGOT_SERVER_NAME)

        val queuedWarp = queuedWarpRepository.get(playerUUID)
        if (queuedWarp == null) {
            logger.debug("No queued warp for $playerUUID")
            return
        }
        if (queuedWarp.serverName == serverName) {
            logger.debug("Found queued warp request for $playerUUID -> $queuedWarp")

            queuedWarpRepository.dequeue(playerUUID)

            val world = event.player.server.getWorld(queuedWarp.worldName)
            if (world == null) {
                logger.warning("Could not find ${queuedWarp.worldName} world to warp to")
                event.player.send().error("Could not find ${queuedWarp.worldName} world")
                return
            }

            val location = Location(
                world,
                queuedWarp.x,
                queuedWarp.y,
                queuedWarp.z,
                queuedWarp.yaw,
                queuedWarp.pitch,
            )
            event.spawnLocation = location

            logger.debug("Set player's spawn location to $location")
        }
    }
}