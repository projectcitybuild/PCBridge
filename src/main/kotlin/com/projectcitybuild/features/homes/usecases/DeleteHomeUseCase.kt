package com.projectcitybuild.features.homes.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.HomeRepository
import java.util.UUID
import javax.inject.Inject

class DeleteHomeUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    enum class FailureReason {
        HOME_NOT_FOUND,
    }

    fun deleteHome(playerUUID: UUID, homeName: String): Result<Unit, FailureReason> {
        if (!homeRepository.exists(homeName, playerUUID)) {
            return Failure(FailureReason.HOME_NOT_FOUND)
        }
        // TODO: Add confirmation
        homeRepository.delete(homeName, playerUUID)

        return Success(Unit)
    }
}