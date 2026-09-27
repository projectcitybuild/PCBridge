package com.projectcitybuild.pcbridge.paper.runtime.permissions

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import org.bukkit.entity.Player

fun Player.hasPermission(permission: PermissionNode) = hasPermission(permission.node)
