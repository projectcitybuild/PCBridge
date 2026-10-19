package com.projectcitybuild.pcbridge.features.building

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object BuildingPermissions {
    // Can toggle night-vision mode
    val NIGHT_VISION = PermissionNode("pcbridge.build.nightvision")

    // Can rename an item via a command
    val ITEM_RENAME = PermissionNode("pcbridge.items.rename")

    // Can get an invisible item frame via a command
    val INVIS_FRAME = PermissionNode("pcbridge.build.invisframe")
}
