package com.projectcitybuild.features.homes.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.repositories.HomeRepository
import java.util.*
import javax.inject.Inject

class CreateHomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val time: Time,
) {
    enum class FailureReason {
        HOME_ALREADY_EXISTS,
        HOME_LIMIT_REACHED,
    }

    fun createHome(
        playerUUID: UUID,
        homeName: String,
        location: CrossServerLocation
    ): Result<Unit, FailureReason> {
        if (homeRepository.exists(homeName, playerUUID)) {
            return Failure(FailureReason.HOME_ALREADY_EXISTS)
        }

        // TODO: check server's home limit

        homeRepository.add(
            homeName = homeName,
            playerUUID = playerUUID,
            location = location,
            createdAt = time.now(),
        )
        return Success(Unit)
    }
}