package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.ranksync.SyncPlayerGroupService
import java.util.*
import javax.inject.Inject

class SyncPlayerGroupsUseCase @Inject constructor(
    private val syncPlayerGroupService: SyncPlayerGroupService
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
    }

    suspend fun sync(playerUUID: UUID): Result<Unit, FailureReason> {
        try {
            syncPlayerGroupService.execute(playerUUID)
        } catch (e: SyncPlayerGroupService.AccountNotLinkedException) {
            return Failure(FailureReason.ACCOUNT_NOT_LINKED)
        }
        return Success(Unit)
    }
}