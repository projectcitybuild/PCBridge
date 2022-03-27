package com.projectcitybuild.features.homes.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.integrations.internal.crossteleport.LocationTeleporter
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.HomeRepository
import org.bukkit.entity.Player
import javax.inject.Inject

class HomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val nameGuesser: NameGuesser,
    private val locationTeleporter: LocationTeleporter,
) {
    enum class FailureReason {
        NO_HOMES_REGISTERED,
        HOME_NOT_FOUND,
        WORLD_NOT_FOUND,
    }

    fun teleportToHome(
        player: Player,
        homeName: String,
    ): Result<Unit, FailureReason> {
        val availableHomeNames = homeRepository.names(player.uniqueId)

        if (availableHomeNames.isEmpty()) {
            return Failure(FailureReason.NO_HOMES_REGISTERED)
        }

        val matchingWarpName = nameGuesser.guessClosest(homeName, availableHomeNames)
            ?: return Failure(FailureReason.HOME_NOT_FOUND)

        val home = homeRepository.first(name = matchingWarpName, playerUUID = player.uniqueId)
            ?: return Failure(FailureReason.HOME_NOT_FOUND)

        val result = locationTeleporter.teleport(
            player = player,
            destination = home.location,
            destinationName = home.name,
        )
        return when (result) {
            is Failure -> Failure(result.reason.bubble())
            is Success -> Success(Unit)
        }
    }
}

private fun LocationTeleporter.FailureReason.bubble(): HomeUseCase.FailureReason {
    return when (this) {
        LocationTeleporter.FailureReason.WORLD_NOT_FOUND -> HomeUseCase.FailureReason.WORLD_NOT_FOUND
    }
}