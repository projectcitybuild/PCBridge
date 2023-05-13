package com.projectcitybuild.features.warnings.usecases

import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.PlayerWarningRepository

class AcknowledgeWarning(
    private val playerWarningRepository: PlayerWarningRepository,
) {
    suspend fun execute(warningId: Int): Result<Unit, Unit> {
        playerWarningRepository.acknowledge(warningId = warningId)
        return Success(Unit)
    }
}
