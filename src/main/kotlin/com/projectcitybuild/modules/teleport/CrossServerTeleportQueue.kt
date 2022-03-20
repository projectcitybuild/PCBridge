package com.projectcitybuild.modules.teleport

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.teleport.repositories.QueuedLocationTeleportRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import javax.inject.Inject

class CrossServerTeleportQueue @Inject constructor(
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
    private val server: Server,
    private val queuedLocationTeleportRepository: QueuedLocationTeleportRepository,
) {
    sealed class Destination {
        data class Location(val location: org.bukkit.Location, val name: String): Destination()
        data class Player(val playerName: String): Destination()
    }

    fun getQueuedTeleport(player: Player): Destination? {
        val playerUUID = player.uniqueId
        val serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME)

        val destination = queuedLocationTeleportRepository.get(playerUUID)
        if (destination == null) {
            logger.debug("No queued warp for $playerUUID")
            return null
        }
        if (destination.location.serverName != serverName) {
            return null
        }

        logger.debug("Found queued warp request for $playerUUID -> $destination")

        queuedLocationTeleportRepository.dequeue(playerUUID)

        val world = server.getWorld(destination.location.worldName)
        if (world == null) {
            logger.warning("Could not find ${destination.location.worldName} world to warp to")
            player.send().error("Could not find ${destination.location.worldName} world")
            return null
        }

        return Destination.Location(
            location = Location(
                world,
                destination.location.x,
                destination.location.y,
                destination.location.z,
                destination.location.yaw,
                destination.location.pitch,
            ),
            name = destination.name,
        )
    }
}