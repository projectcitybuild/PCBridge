package com.projectcitybuild.features.hub.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.HubRepository
import com.projectcitybuild.shared.locationteleport.LocationTeleporter
import org.bukkit.entity.Player
import javax.inject.Inject

class HubTeleportUseCase @Inject constructor(
    private val hubRepository: HubRepository,
    private val locationTeleporter: LocationTeleporter,
) {
    enum class FailureReason {
        NO_HUB_EXISTS,
        WORLD_NOT_FOUND,
    }

    fun execute(player: Player): Result<Unit, FailureReason> {
        val hubLocation = hubRepository.get()
            ?: return Failure(FailureReason.NO_HUB_EXISTS)

        val result = locationTeleporter.teleport(
            player = player,
            destination = hubLocation,
        )
        if (result is Failure) {
            when (result.reason) {
                LocationTeleporter.FailureReason.WORLD_NOT_FOUND ->
                    return Failure(FailureReason.WORLD_NOT_FOUND)
            }
        }
        return Success(Unit)
    }
}
