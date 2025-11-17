package com.projectcitybuild.pcbridge.paper.architecture.permissions

import com.projectcitybuild.pcbridge.paper.PermissionNode
import org.bukkit.entity.Player

fun Player.hasPermission(permission: PermissionNode)
    = hasPermission(permission.node)