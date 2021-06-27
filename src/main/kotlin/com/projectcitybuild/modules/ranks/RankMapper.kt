package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.entities.models.Group
import com.projectcitybuild.platforms.spigot.listeners.ChatListener

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
                "retired" -> permissionGroups.add("retired")
                "moderator" -> permissionGroups.add("moderator")
                "operator" -> permissionGroups.add("op")
                "senior operator" -> permissionGroups.add("sop")
                "administrator" -> permissionGroups.add("admin")
                "intern" -> permissionGroups.add("intern")
                "builder" -> permissionGroups.add("builder")
                "planner" -> permissionGroups.add("planner")
                "engineer" -> permissionGroups.add("engineer")
                "architect" -> permissionGroups.add("architect")
            }
        }

        // User cannot be a Member if they're in any other group
//        if (permissionGroups.contains("Member") && permissionGroups.size > 1) {
//            permissionGroups.remove("Member")
//        }

        return permissionGroups.toList()
    }

}
