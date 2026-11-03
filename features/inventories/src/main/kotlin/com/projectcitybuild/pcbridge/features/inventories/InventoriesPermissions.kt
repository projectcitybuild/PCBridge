package com.projectcitybuild.pcbridge.features.inventories

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object InventoriesPermissions {
    val MANAGE = PermissionNode("pcbridge.inventories.manage")
    val BYPASS_WORLD = PermissionNode("pcbridge.inventories.bypass.world")
    val BYPASS_GROUP = PermissionNode("pcbridge.inventories.bypass.group")
    val BYPASS_GAME_MODE = PermissionNode("pcbridge.inventories.bypass.gamemode")
}
