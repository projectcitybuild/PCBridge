package com.projectcitybuild.pcbridge.paper.features.groups.actions

import com.projectcitybuild.pcbridge.paper.core.permissions.Permissions
import com.projectcitybuild.pcbridge.http.models.pcb.Group
import java.util.UUID

class SyncPlayerGroups(
    private val permissions: Permissions,
) {
    fun execute(
        playerUUID: UUID,
        groups: List<Group>,
    ) {
        val groupSet = groups.map { it.minecraftName }.toSet()
        permissions.setUserGroups(playerUUID, groupSet.toList().requireNoNulls())
    }
}
