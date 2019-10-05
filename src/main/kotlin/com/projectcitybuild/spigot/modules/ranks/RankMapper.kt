package com.projectcitybuild.spigot.modules.ranks

import com.projectcitybuild.entities.models.Group

object RankMapper {

    fun mapGroupsToPermissionGroups(groups: List<Group>): List<String> {
        val permissionGroups = mutableSetOf<String>()

        // TODO: [@andy] use Config file instead of hardcoding these
        groups.forEach { group ->
            when (group.name) {
                "member" -> permissionGroups.add("member")
                "donator" -> permissionGroups.add("donator")
                "trusted" -> permissionGroups.add("trusted")
                "trusted plus" -> permissionGroups.add("trusted+")
                "moderator" -> permissionGroups.add("moderator")
                "operator" -> permissionGroups.add("op")
                "senior operator" -> permissionGroups.add("sop")
                "administrator" -> permissionGroups.add("admin")
            }
        }

        // User cannot be a Member if they're in any other group
        if (permissionGroups.contains("Member") && permissionGroups.size > 1) {
            permissionGroups.remove("Member")
        }

        return permissionGroups.toList()
    }

}