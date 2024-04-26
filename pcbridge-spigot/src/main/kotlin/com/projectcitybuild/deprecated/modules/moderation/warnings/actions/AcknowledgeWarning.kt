package com.projectcitybuild.modules.moderation.warnings.actions

import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.repositories.PlayerWarningRepository

class AcknowledgeWarning(
    private val playerWarningRepository: PlayerWarningRepository,
) {
    suspend fun execute(warningId: Int): Result<Unit, Unit> {
        playerWarningRepository.acknowledge(warningId = warningId)
        return Success(Unit)
    }
}
