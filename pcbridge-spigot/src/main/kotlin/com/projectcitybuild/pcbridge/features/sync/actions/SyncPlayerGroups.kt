package com.projectcitybuild.pcbridge.features.sync.actions

import com.projectcitybuild.pcbridge.core.permissions.Permissions
import com.projectcitybuild.pcbridge.features.sync.repositories.SyncRepository
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.responses.Group
import java.util.UUID

class SyncPlayerGroups(
    private val permissions: Permissions,
    private val syncRepository: SyncRepository,
) {
    fun execute(
        playerUUID: UUID,
        groups: List<Group>,
        donationPerks: List<DonationPerk>,
    ) {
        val groupSet = mutableSetOf<String>()
        groupSet.addAll(
            groups.mapNotNull { it.minecraftName }
        )
        groupSet.addAll(
            syncRepository.getDonorTiers(donationPerks),
        )
        permissions.setUserGroups(playerUUID, groupSet.toList())
    }
}
