package com.projectcitybuild.pcbridge.features.teleportation.services

data class TeleportFlags(
    // Bypasses teleportation consent checks, allowing the teleportation
    // to occur without the target's approval.
    val force: Boolean = false,

    // Suppresses any notifications or messages that would normally be sent
    // to the target player.
    val silent: Boolean = false,
)
