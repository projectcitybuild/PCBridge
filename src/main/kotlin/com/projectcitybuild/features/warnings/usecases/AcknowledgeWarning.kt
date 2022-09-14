package com.projectcitybuild.features.warnings.usecases

import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.PlayerWarningRepository
import javax.inject.Inject

class AcknowledgeWarning @Inject constructor(
    private val playerWarningRepository: PlayerWarningRepository,
) {
    suspend fun execute(warningId: Int): Result<Unit, Unit> {
        playerWarningRepository.acknowledge(warningId = warningId)
        return Success(Unit)
    }
}
