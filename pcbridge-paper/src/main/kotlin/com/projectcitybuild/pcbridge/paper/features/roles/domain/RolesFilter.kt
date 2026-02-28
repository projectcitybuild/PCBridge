package com.projectcitybuild.pcbridge.paper.features.roles.domain

import com.projectcitybuild.pcbridge.http.pcb.models.Role
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.roles.domain.data.RoleType

class RolesFilter {
    /**
     * Sorts the given collection of roles into a Map, where only
     * the highest display priority of a RoleType is present
     */
    fun filter(roles: Set<Role>): Map<RoleType, Role> {
        if (roles.isEmpty()) {
            return emptyMap()
        }
        val mapping = mutableMapOf<RoleType, Role>()
        for (role in roles) {
            val rawRoleType = role.roleType
            val displayPriority = role.displayPriority

            if (rawRoleType == null || displayPriority == null) continue

            val roleType = runCatching { RoleType.valueOf(rawRoleType.uppercase()) }.getOrNull()
            if (roleType == null) {
                logSync.error { "$rawRoleType is not a recognized role type" }
                continue
            }
            val existing = mapping[roleType]
            if (existing == null || existing.displayPriority!! < displayPriority) {
                mapping[roleType] = role
            }
        }
        return mapping
    }
}