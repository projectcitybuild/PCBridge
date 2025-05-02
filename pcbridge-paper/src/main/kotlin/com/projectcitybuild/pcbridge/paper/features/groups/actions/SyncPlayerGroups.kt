package com.projectcitybuild.pcbridge.paper.features.groups.actions

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.Permissions
import java.util.UUID

class SyncPlayerGroups(
    private val permissions: Permissions,
) {
    fun execute(
        playerUUID: UUID,
        groups: List<Group>,
    ) {
        val groupSet = groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(playerUUID, groupSet)
    }
}
