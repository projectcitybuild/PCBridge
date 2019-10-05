package com.projectcitybuild.spigot.modules.ranks

import com.projectcitybuild.entities.models.Group

object RankMapper {

    fun mapGroupsToPermissionGroups(groups: List<Group>): List<String> {
        val permissionGroups = mutableSetOf<String>()

        // TODO: [@andy] use Config file instead of hardcoding these
        groups.forEach { group ->
            when (group.name) {
                "member" -> permissionGroups.add("Member")
                "donator" -> permissionGroups.add("Donator")
                "trusted" -> permissionGroups.add("Trusted")
                "trusted plus" -> permissionGroups.add("Trusted+")
                "moderator" -> permissionGroups.add("Moderator")
                "operator" -> permissionGroups.add("OP")
                "senior operator" -> permissionGroups.add("SOP")
                "administrator" -> permissionGroups.add("Admin")
            }
        }

        // User cannot be a Member if they're in any other group
        if (permissionGroups.contains("Member") && permissionGroups.size > 1) {
            permissionGroups.remove("Member")
        }

        return permissionGroups.toList()
    }

}