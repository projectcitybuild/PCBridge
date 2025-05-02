package com.projectcitybuild.pcbridge.paper.core.libs.permissions

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import java.util.UUID

class BasicPermissionsProvider: PermissionsProvider {
    override fun getUserRoles(playerUUID: UUID): Set<String> {
        log.error { "User roles are not supported with the fallback permissions provider" }
        return emptySet()
    }

    override fun setUserRoles(playerUUID: UUID, roleNames: Set<String>) {
        log.error { "User roles are not supported with the fallback permissions provider" }
    }
}