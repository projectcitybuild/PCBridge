package com.projectcitybuild.modules.permissions

import java.util.*

interface Permissions {
    fun bootstrap()
    fun setUserGroups(playerUUID: UUID, groupNames: List<String>)
}
