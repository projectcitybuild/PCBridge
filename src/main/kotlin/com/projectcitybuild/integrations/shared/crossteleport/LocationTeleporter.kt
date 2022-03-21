package com.projectcitybuild.integrations.shared.crossteleport

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.integrations.shared.crossteleport.events.PlayerPreLocationTeleportEvent
import com.projectcitybuild.modules.channels.ProxyMessenger
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.repositories.QueuedLocationTeleportRepository
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import javax.inject.Inject

class LocationTeleporter @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val queuedLocationTeleportRepository: QueuedLocationTeleportRepository,
    private val server: Server,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
    private val proxyMessenger: ProxyMessenger,
) {
    enum class DestinationType {
        SAME_SERVER,
        CROSS_SERVER,
    }

    enum class FailureReason {
        WORLD_NOT_FOUND,
    }

    /**
     * Attempts to teleport the player to a given location regardless of which
     * server the destination is in.
     *
     * If the player is in the same server as the destination, the teleport will
     * be instantaneous. If the server is different, the player will be transferred
     * to the appropriate server and then teleported upon arrival
     *
     * @param player The player to teleport
     * @param destination Location to teleport to
     * @param destinationName Name shown to the player upon successfully teleporting
     *
     * @see CrossServerTeleportQueue.dequeue
     */
    fun teleport(
        player: Player,
        destination: CrossServerLocation,
        destinationName: String,
    ): Result<DestinationType, FailureReason> {
        val serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME)
        val isWarpOnSameServer = serverName == destination.serverName

        return if (isWarpOnSameServer) {
            warpImmediately(player, destination)
        } else {
            queueWarpThenTransferPlayer(player, destination, destinationName)
        }
    }

    private fun warpImmediately(player: Player, destination: CrossServerLocation): Result<DestinationType, FailureReason> {
        val world = server.getWorld(destination.worldName)
        if (world == null) {
            logger.warning("Could not find world matching name [${destination.worldName}] for warp")
            return Failure(FailureReason.WORLD_NOT_FOUND)
        }
        localEventBroadcaster.emit(
            PlayerPreLocationTeleportEvent(player, player.location)
        )
        player.teleport(
            Location(
                world,
                destination.x,
                destination.y,
                destination.z,
                destination.yaw,
                destination.pitch,
            )
        )
        return Success(DestinationType.SAME_SERVER)
    }

    private fun queueWarpThenTransferPlayer(
        player: Player,
        destination: CrossServerLocation,
        destinationName: String,
    ): Result<DestinationType, FailureReason> {
        queuedLocationTeleportRepository.queue(
            player.uniqueId,
            destinationName,
            destination
        )
        localEventBroadcaster.emit(
            PlayerPreLocationTeleportEvent(player, player.location)
        )
        proxyMessenger.sendToProxy(
            sender = player,
            subChannel = SubChannel.SWITCH_PLAYER_SERVER,
            params = arrayOf(
                player.uniqueId.toString(),
                destination.serverName,
            ),
        )

        return Success(DestinationType.CROSS_SERVER)
    }
}