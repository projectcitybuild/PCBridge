package com.projectcitybuild.pcbridge.paper.features.groups.actions

import com.projectcitybuild.pcbridge.paper.core.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.GroupRepository
import com.projectcitybuild.pcbridge.http.models.DonationPerk
import com.projectcitybuild.pcbridge.http.models.Group
import java.util.UUID

class SyncPlayerGroups(
    private val permissions: Permissions,
    private val groupRepository: GroupRepository,
) {
    fun execute(
        playerUUID: UUID,
        groups: List<Group>,
        donationPerks: List<DonationPerk>,
    ) {
        val groupSet = mutableSetOf<String>()
        groupSet.addAll(
            groups.mapNotNull { it.minecraftName },
        )
        groupSet.addAll(
            groupRepository.getDonorTiers(donationPerks),
        )
        permissions.setUserGroups(playerUUID, groupSet.toList())
    }
}
