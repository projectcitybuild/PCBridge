package com.projectcitybuild.modules.permissions

import java.util.*

interface Permissions {
    fun bootstrap()

    fun setUserGroups(playerUUID: UUID, groupNames: List<String>)
    fun getUserGroups(playerUUID: UUID): Set<String>
    fun getUserPrefix(playerUUID: UUID): String
    fun getUserSuffix(playerUUID: UUID): String

    fun getGroupDisplayName(groupName: String): String?
}