package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.LastKnownLocationRepository
import com.projectcitybuild.repositories.WarpRepository
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import javax.inject.Inject

class TeleportToWarpUseCase @Inject constructor(
    private val warpRepository: WarpRepository,
    private val nameGuesser: NameGuesser,
    private val logger: PlatformLogger,
    private val lastKnownLocationRepository: LastKnownLocationRepository,
    private val server: Server,
) {
    data class WarpEvent(
        val warpName: String,
    )
    enum class FailureReason {
        WARP_NOT_FOUND,
        WORLD_NOT_FOUND,
    }

    fun warp(
        player: Player,
        targetWarpName: String,
    ): Result<WarpEvent, FailureReason> {
        val availableWarpNames = warpRepository.names()

        val matchingWarpName = nameGuesser.guessClosest(targetWarpName, availableWarpNames)
            ?: return Failure(FailureReason.WARP_NOT_FOUND)

        val warp = warpRepository.first(matchingWarpName)
            ?: return Failure(FailureReason.WARP_NOT_FOUND)

        val world = server.getWorld(warp.location.worldName)
        if (world == null) {
            logger.warning("Could not find world matching name [${warp.location.worldName}] for warp")
            return Failure(FailureReason.WORLD_NOT_FOUND)
        }

        lastKnownLocationRepository.updateLastKnownLocation(player = player)

        player.teleport(
            Location(
                world,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                warp.location.yaw,
                warp.location.pitch,
            )
        )
        return Success(WarpEvent(warpName = warp.name))
    }
}
