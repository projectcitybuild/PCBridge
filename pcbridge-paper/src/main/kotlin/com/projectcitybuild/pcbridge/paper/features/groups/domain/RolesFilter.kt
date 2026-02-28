package com.projectcitybuild.pcbridge.paper.features.groups.domain

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.groups.domain.data.RoleType

class RolesFilter {
    /**
     * Sorts the given collection of roles into a Map, where only
     * the highest display priority of a RoleType is present
     */
    fun filter(groups: Set<Group>): Map<RoleType, Group> {
        if (groups.isEmpty()) {
            return emptyMap()
        }
        val mapping = mutableMapOf<RoleType, Group>()
        for (group in groups) {
            val rawRoleType = group.roleType
            val displayPriority = group.displayPriority

            if (rawRoleType == null || displayPriority == null) continue

            val roleType = runCatching { RoleType.valueOf(rawRoleType.uppercase()) }.getOrNull()
            if (roleType == null) {
                logSync.error { "$rawRoleType is not a recognized role type" }
                continue
            }
            val existing = mapping[roleType]
            if (existing == null || existing.displayPriority!! < displayPriority) {
                mapping[roleType] = group
            }
        }
        return mapping
    }
}