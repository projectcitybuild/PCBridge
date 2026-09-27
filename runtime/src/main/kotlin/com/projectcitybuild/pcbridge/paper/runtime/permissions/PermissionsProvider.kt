package com.projectcitybuild.pcbridge.paper.runtime.permissions

import java.util.UUID

interface PermissionsProvider {
    fun getUserRoles(playerUUID: UUID): Set<String>

    fun setUserRoles(
        playerUUID: UUID,
        roleNames: Set<String>,
    )
}
