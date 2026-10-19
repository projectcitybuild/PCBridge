package com.projectcitybuild.pcbridge.features.builds

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object BuildsPermissions {
    // Can teleport to a build
    val TELEPORT = PermissionNode("pcbridge.builds.teleport")

    // Can create, edit or delete a build
    val MANAGE = PermissionNode("pcbridge.builds.manage")

    // Can vote for a build
    val VOTE = PermissionNode("pcbridge.builds.vote")
}
