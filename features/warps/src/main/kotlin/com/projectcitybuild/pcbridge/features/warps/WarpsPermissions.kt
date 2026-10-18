package com.projectcitybuild.pcbridge.features.warps

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object WarpsPermissions {
    // Can teleport to a warp
    val TELEPORT = PermissionNode("pcbridge.warp.teleport")

    // Can create, edit or delete warps
    val MANAGE = PermissionNode("pcbridge.warp.manage")
}
