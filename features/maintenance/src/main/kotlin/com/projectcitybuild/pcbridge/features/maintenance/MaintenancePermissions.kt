package com.projectcitybuild.pcbridge.features.maintenance

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object MaintenancePermissions {
    // Can toggle maintenance mode on or off
    val MANAGE = PermissionNode("pcbridge.maintenance.manage")
}
