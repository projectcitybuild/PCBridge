package com.projectcitybuild.pcbridge.paper.core.permissions

import java.util.UUID

interface Permissions {
    fun setUserGroups(
        playerUUID: UUID,
        groupNames: List<String>,
    )

    fun getUserGroups(playerUUID: UUID): Set<String>
}
