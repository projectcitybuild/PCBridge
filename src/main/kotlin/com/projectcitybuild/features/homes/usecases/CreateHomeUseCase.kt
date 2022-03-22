package com.projectcitybuild.features.homes.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.repositories.HomeRepository
import java.util.UUID
import javax.inject.Inject

class CreateHomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val config: PlatformConfig,
    private val time: Time,
) {
    enum class FailureReason {
        HOME_ALREADY_EXISTS,
        HOME_LIMIT_REACHED,
        HOME_NOT_ALLOWED_IN_WORLD,
    }

    fun createHome(
        playerUUID: UUID,
        homeName: String,
        location: CrossServerLocation
    ): Result<Unit, FailureReason> {
        if (homeRepository.exists(homeName, playerUUID)) {
            return Failure(FailureReason.HOME_ALREADY_EXISTS)
        }

        val worldName = location.worldName
        val worldHomeLimit = config.get("homes.limits.$worldName")?.let { it as? Int } ?: 0
        if (worldHomeLimit == 0) {
            return Failure(FailureReason.HOME_NOT_ALLOWED_IN_WORLD)
        }

        val numberOfHomesInWorld = homeRepository.count(
            playerUUID = playerUUID,
            serverName = location.serverName,
            worldName = location.worldName,
        )
        if (numberOfHomesInWorld >= worldHomeLimit) {
            return Failure(FailureReason.HOME_LIMIT_REACHED)
        }

        homeRepository.add(
            homeName = homeName,
            playerUUID = playerUUID,
            location = location,
            createdAt = time.now(),
        )
        return Success(Unit)
    }
}