package com.projectcitybuild.features.warps.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.teleport.repositories.QueuedLocationTeleportRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import javax.inject.Inject

class WarpOnJoinListener @Inject constructor(
    private val queuedLocationTeleportRepository: QueuedLocationTeleportRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
): SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val playerUUID = event.player.uniqueId
        val serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME)

        val queuedWarp = queuedLocationTeleportRepository.get(playerUUID)
        if (queuedWarp == null) {
            logger.debug("No queued warp for $playerUUID")
            return
        }
        if (queuedWarp.location.serverName != serverName) {
            return
        }

        logger.debug("Found queued warp request for $playerUUID -> $queuedWarp")

        queuedLocationTeleportRepository.dequeue(playerUUID)

        val world = event.player.server.getWorld(queuedWarp.location.worldName)
        if (world == null) {
            logger.warning("Could not find ${queuedWarp.location.worldName} world to warp to")
            event.player.send().error("Could not find ${queuedWarp.location.worldName} world")
            return
        }

        val location = Location(
            world,
            queuedWarp.location.x,
            queuedWarp.location.y,
            queuedWarp.location.z,
            queuedWarp.location.yaw,
            queuedWarp.location.pitch,
        )
        event.spawnLocation = location

        logger.debug("Set player's spawn location to $location")

        event.player.send().action("Warped to ${queuedWarp.name}")
    }
}