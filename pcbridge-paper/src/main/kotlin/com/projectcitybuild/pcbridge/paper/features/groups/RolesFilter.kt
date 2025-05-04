package com.projectcitybuild.pcbridge.paper.features.groups

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log

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
            val rawRoleType = group.groupType
            val displayPriority = group.displayPriority

            if (rawRoleType == null || displayPriority == null) continue;

            val roleType = runCatching { RoleType.valueOf(rawRoleType) }.getOrNull()
            if (roleType == null) {
                log.error { "$rawRoleType is not a recoginzed role type" }
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