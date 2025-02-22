package com.projectcitybuild.pcbridge.paper.core.libs.permissions

import java.util.UUID

interface Permissions {
    fun setUserGroups(
        playerUUID: UUID,
        groupNames: Set<String>,
    )

    fun getUserGroups(playerUUID: UUID): Set<String>
}
