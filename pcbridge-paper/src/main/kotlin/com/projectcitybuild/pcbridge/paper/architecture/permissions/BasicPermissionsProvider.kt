package com.projectcitybuild.pcbridge.paper.architecture.permissions

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import java.util.UUID

class BasicPermissionsProvider: PermissionsProvider {
    override fun getUserRoles(playerUUID: UUID): Set<String> {
        logSync.warn { "BasicPermissionsProvider does not support user roles" }
        return emptySet()
    }

    override fun setUserRoles(playerUUID: UUID, roleNames: Set<String>) {
        logSync.warn { "BasicPermissionsProvider does not support user roles" }
    }
}