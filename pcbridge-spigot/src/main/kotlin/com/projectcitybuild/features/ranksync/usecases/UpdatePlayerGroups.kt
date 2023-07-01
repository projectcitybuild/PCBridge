package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.repositories.PlayerGroupRepository
import java.util.UUID

class UpdatePlayerGroups(
    private val permissions: Permissions,
    private val playerGroupRepository: PlayerGroupRepository,
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
    }

    suspend fun execute(playerUUID: UUID): Result<Unit, FailureReason> {
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
