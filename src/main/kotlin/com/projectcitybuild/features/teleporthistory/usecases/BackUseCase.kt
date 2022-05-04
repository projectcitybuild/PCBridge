package com.projectcitybuild.features.teleporthistory.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.shared.locationteleport.LocationTeleporter
import org.bukkit.entity.Player
import javax.inject.Inject

class BackUseCase @Inject constructor(
    private val lastKnownLocationRepositoy: LastKnownLocationRepositoy,
    private val locationTeleporter: LocationTeleporter,
) {
    enum class FailureReason {
        NO_LAST_LOCATION,
        WORLD_NOT_FOUND,
    }

    fun teleportBack(player: Player): Result<Unit, FailureReason> {
        val lastKnownLocation = lastKnownLocationRepositoy.get(player.uniqueId)
            ?: return Failure(FailureReason.NO_LAST_LOCATION)

        val result = locationTeleporter.teleport(
            player = player,
            destination = lastKnownLocation.location,
        )
        return when (result) {
            is Failure -> Failure(result.reason.bubble())
            is Success -> Success(Unit)
        }
    }
}

private fun LocationTeleporter.FailureReason.bubble(): BackUseCase.FailureReason {
    return when (this) {
        LocationTeleporter.FailureReason.WORLD_NOT_FOUND -> BackUseCase.FailureReason.WORLD_NOT_FOUND
    }
}
