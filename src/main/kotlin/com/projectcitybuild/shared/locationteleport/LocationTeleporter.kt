package com.projectcitybuild.shared.locationteleport

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.plugin.events.PlayerPreLocationTeleportEvent
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import javax.inject.Inject

class LocationTeleporter @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val server: Server,
    private val logger: PlatformLogger,
) {
    enum class FailureReason {
        WORLD_NOT_FOUND,
    }

    fun teleport(
        player: Player,
        destination: CrossServerLocation,
    ): Result<Unit, FailureReason> {
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
        return Success(Unit)
    }
}
