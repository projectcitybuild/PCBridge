package com.projectcitybuild.pcbridge.paper.architecture.permissions

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import java.util.UUID

class BasicPermissionsProvider: PermissionsProvider {
    override fun getUserRoles(playerUUID: UUID): Set<String> {
        log.warn { "BasicPermissionsProvider does not support user roles" }
        return emptySet()
    }

    override fun setUserRoles(playerUUID: UUID, roleNames: Set<String>) {
        log.warn { "BasicPermissionsProvider does not support user roles" }
    }
}