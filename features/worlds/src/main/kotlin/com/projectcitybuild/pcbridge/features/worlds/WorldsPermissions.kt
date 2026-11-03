package com.projectcitybuild.pcbridge.features.worlds

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object WorldsPermissions {
    val MANAGE = PermissionNode("pcbridge.worlds.manage")
    val LIFECYCLE = PermissionNode("pcbridge.worlds.lifecycle")
    val DELETE = PermissionNode("pcbridge.worlds.delete")
}
