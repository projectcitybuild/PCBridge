package com.projectcitybuild.wiring.shared.crossteleport

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import com.projectcitybuild.repositories.QueuedLocationTeleportRepository
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import javax.inject.Inject

class LocationTeleporter @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val queuedLocationTeleportRepository: QueuedLocationTeleportRepository,
    private val plugin: Plugin,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
) {
    enum class TeleportType {
        SAME_SERVER,
        CROSS_SERVER,
    }

    enum class FailureReason {
        WORLD_NOT_FOUND,
    }

    fun teleport(
        player: Player,
        destination: CrossServerLocation,
        destinationName: String,
    ): Result<TeleportType, FailureReason> {
        localEventBroadcaster.emit(
            PlayerPreWarpEvent(player, player.location)
        )
        val serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME)
        val isWarpOnSameServer = serverName == destination.serverName
        if (isWarpOnSameServer) {
            val worldName = destination.worldName
            val world = plugin.server.getWorld(worldName)
            if (world == null) {
                logger.warning("Could not find world matching name [$worldName] for warp")
                return Failure(FailureReason.WORLD_NOT_FOUND)
            }
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
        } else {
            queuedLocationTeleportRepository.queue(
                player.uniqueId,
                destinationName,
                destination
            )
            MessageToBungeecord(
                plugin,
                player,
                SubChannel.SWITCH_PLAYER_SERVER,
                arrayOf(
                    player.uniqueId.toString(),
                    destination.serverName,
                )
            ).send()
        }

        return Success(
            if (isWarpOnSameServer) TeleportType.SAME_SERVER
            else TeleportType.CROSS_SERVER
        )
    }
}