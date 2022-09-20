package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.repositories.PlayerGroupRepository
import java.util.UUID
import javax.inject.Inject

class UpdatePlayerGroups @Inject constructor(
    private val permissions: Permissions,
    private val playerGroupRepository: PlayerGroupRepository,
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
    }

    suspend fun sync(playerUUID: UUID): Result<Unit, FailureReason> {
        val groupSet = mutableSetOf<String>()

        try {
            groupSet.addAll(playerGroupRepository.getGroups(playerUUID = playerUUID))
            groupSet.addAll(playerGroupRepository.getDonorTiers(playerUUID = playerUUID))
        } catch (e: PlayerGroupRepository.AccountNotLinkedException) {
            return Failure(FailureReason.ACCOUNT_NOT_LINKED)
        }

        permissions.setUserGroups(playerUUID, groupSet.toList())

        return Success(Unit)
    }
}
