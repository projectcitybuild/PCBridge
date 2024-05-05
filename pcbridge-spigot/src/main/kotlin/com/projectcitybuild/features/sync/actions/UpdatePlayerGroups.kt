package com.projectcitybuild.features.sync.actions

import com.projectcitybuild.core.permissions.Permissions
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import com.projectcitybuild.features.sync.repositories.SyncRepository
import java.util.UUID

class UpdatePlayerGroups(
    private val permissions: Permissions,
    private val syncRepository: SyncRepository,
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
    }

    suspend fun execute(playerUUID: UUID): Result<Unit, FailureReason> {
        val groupSet = mutableSetOf<String>()

        try {
            groupSet.addAll(syncRepository.getGroups(playerUUID = playerUUID))

            val donorPerks = syncRepository.getDonorPerks(playerUUID)
            val donorTierGroups = syncRepository.getDonorTiers(donorPerks)
            groupSet.addAll(donorTierGroups)
        } catch (e: PlayerGroupHttpService.NoLinkedAccountException) {
            return Failure(FailureReason.ACCOUNT_NOT_LINKED)
        }

        permissions.setUserGroups(playerUUID, groupSet.toList())

        return Success(Unit)
    }
}
