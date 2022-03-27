package com.projectcitybuild.modules.permissions

import java.util.UUID

interface Permissions {
    fun connect()

    fun setUserGroups(playerUUID: UUID, groupNames: List<String>)
    fun getUserGroups(playerUUID: UUID): Set<String>
    fun getUserPrefix(playerUUID: UUID): String
    fun getUserSuffix(playerUUID: UUID): String
    fun <T> getUserMetadata(playerUUID: UUID, key: String, valueTransformer: (String) -> T): T?

    fun getGroupDisplayName(groupName: String): String?

    fun hasPermission(playerUUID: UUID, permissionNode: String): Boolean
}
