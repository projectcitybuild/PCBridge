// package com.projectcitybuild.pcbridge.features.sync.actions
//
// import com.projectcitybuild.pcbridge.core.permissions.Permissions
// import com.projectcitybuild.pcbridge.features.sync.repositories.SyncRepository
// import com.projectcitybuild.pcbridge.utils.Failure
// import com.projectcitybuild.pcbridge.utils.Result
// import com.projectcitybuild.pcbridge.utils.Success
// import java.util.UUID
//
// class UpdatePlayerGroups(
//     private val permissions: Permissions,
//     private val syncRepository: SyncRepository,
// ) {
//     enum class FailureReason {
//         ACCOUNT_NOT_LINKED,
//     }
//
//     suspend fun execute(playerUUID: UUID): Result<Unit, FailureReason> {
//         val groupSet = mutableSetOf<String>()
//
//         try {
//             groupSet.addAll(syncRepository.getGroups(playerUUID = playerUUID))
//
//             val donorPerks = syncRepository.getDonorPerks(playerUUID)
//             val donorTierGroups = syncRepository.getDonorTiers(donorPerks)
//             groupSet.addAll(donorTierGroups)
//         } catch (e: PlayerGroupHttpService.NoLinkedAccountException) {
//             return Failure(FailureReason.ACCOUNT_NOT_LINKED)
//         }
//
//         permissions.setUserGroups(playerUUID, groupSet.toList())
//
//         return Success(Unit)
//     }
// }
