package com.projectcitybuild.integrations.shared.crossteleport

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.repositories.QueuedLocationTeleportRepository
import com.projectcitybuild.repositories.QueuedPlayerTeleportRepository
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.*
import javax.inject.Inject

class CrossServerTeleportQueue @Inject constructor(
    private val config: PlatformConfig,
    private val server: Server,
    private val queuedLocationTeleportRepository: QueuedLocationTeleportRepository,
    private val queuedPlayerTeleportRepository: QueuedPlayerTeleportRepository,
) {
    sealed class Destination {
        data class Location(
            val location: org.bukkit.Location,
            val name: String,
        ): Destination()

        data class Player(
            val destinationPlayer: org.bukkit.entity.Player,
            val location: org.bukkit.Location,
            val isSummon: Boolean,
            val isSilentTeleport: Boolean,
        ): Destination()
    }

    sealed class FailureReason {
        data class WorldNotFound(val worldName: String): FailureReason()
        object DestinationPlayerNotFound: FailureReason()
    }

    /**
     * Returns and removes a pending cross-server teleport for the given player,
     * if one exists and the target destination matches this server.
     *
     * @see LocationTeleporter.teleport
     */
    fun dequeue(playerUUID: UUID): Result<Destination?, FailureReason> {
        val serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME)

        val destinationLocation = queuedLocationTeleportRepository.get(playerUUID)
        if (destinationLocation != null && destinationLocation.location.serverName == serverName) {
            queuedLocationTeleportRepository.dequeue(playerUUID)

            val world = server.getWorld(destinationLocation.location.worldName)
                ?: return Failure(FailureReason.WorldNotFound(destinationLocation.location.worldName))

            return Success(
                Destination.Location(
                    location = Location(
                        world,
                        destinationLocation.location.x,
                        destinationLocation.location.y,
                        destinationLocation.location.z,
                        destinationLocation.location.yaw,
                        destinationLocation.location.pitch,
                    ),
                    name = destinationLocation.name,
                )
            )
        }

        val queuedPlayerTeleport = queuedPlayerTeleportRepository.get(playerUUID)
        if (queuedPlayerTeleport != null && queuedPlayerTeleport.targetServerName == serverName) {
            queuedPlayerTeleportRepository.dequeue(playerUUID)

            val destinationPlayer = server.getPlayer(queuedPlayerTeleport.targetPlayerUUID)
                ?: return Failure(FailureReason.DestinationPlayerNotFound)

            return Success(
                Destination.Player(
                    destinationPlayer = destinationPlayer,
                    location = destinationPlayer.location,
                    isSummon = queuedPlayerTeleport.teleportType == TeleportType.SUMMON,
                    isSilentTeleport = queuedPlayerTeleport.isSilentTeleport,
                )
            )
        }

        return Success(null)
    }
}