package com.projectcitybuild.pcbridge.features.teleportation

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object TeleportationPermissions {
    // Can teleport to a random location
    val RANDOM = PermissionNode("pcbridge.teleport.random")

    // Can request to teleport to another player
    val REQUEST = PermissionNode("pcbridge.teleport.request")

    // Can bypass teleport requests
    val FORCE = PermissionNode("pcbridge.teleport.force")

    // Can teleport other players
    val OTHERS = PermissionNode("pcbridge.teleport.others")

    // Can teleport all online players
    val ALL = PermissionNode("pcbridge.teleport.all")

    // Can teleport to coordinates
    val POSITION = PermissionNode("pcbridge.teleport.position")

    // Can return to the previous location
    val BACK = PermissionNode("pcbridge.teleport.back")

    // Can return another player to their previous location
    val BACK_OTHERS = PermissionNode("pcbridge.teleport.back.others")

    // Can suppress destination-side teleport notifications
    val SILENT = PermissionNode("pcbridge.teleport.silent")
}
