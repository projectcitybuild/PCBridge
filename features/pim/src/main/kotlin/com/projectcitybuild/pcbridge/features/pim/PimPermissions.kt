package com.projectcitybuild.pcbridge.features.pim

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object PimPermissions {
    // Can temporarily elevate themselves to OP
    val OP = PermissionNode("pcbridge.pim.op")

    // Can debug roles
    val ROLES = PermissionNode("pcbridge.pim.roles")
}
