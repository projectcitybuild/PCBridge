package com.projectcitybuild.pcbridge.runtime.permissions

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import org.bukkit.entity.Player

fun Player.hasPermission(permission: PermissionNode) = hasPermission(permission.node)
