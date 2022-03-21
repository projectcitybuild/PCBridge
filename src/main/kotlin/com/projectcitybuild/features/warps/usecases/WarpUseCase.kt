package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.wiring.shared.crossteleport.LocationTeleporter
import org.bukkit.entity.Player
import javax.inject.Inject

class WarpUseCase @Inject constructor(
    private val warpRepository: WarpRepository,
    private val nameGuesser: NameGuesser,
    private val locationTeleporter: LocationTeleporter,
) {
    data class WarpEvent(
        val warpName: String,
        val isSameServer: Boolean
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

        val result = locationTeleporter.teleport(
            player = player,
            destination = warp.location,
            destinationName = warp.name,
        )
        return when (result) {
            is Failure -> Failure(result.reason.bubble())
            is Success -> Success(
                WarpEvent(
                    warpName = warp.name,
                    isSameServer = result.value == LocationTeleporter.TeleportType.SAME_SERVER,
                )
            )
        }
    }
}

private fun LocationTeleporter.FailureReason.bubble(): WarpUseCase.FailureReason {
    return when (this) {
        LocationTeleporter.FailureReason.WORLD_NOT_FOUND -> WarpUseCase.FailureReason.WORLD_NOT_FOUND
    }
}