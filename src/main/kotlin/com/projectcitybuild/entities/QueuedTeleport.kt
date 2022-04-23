package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.UUID

data class QueuedTeleport(
    val playerUUID: UUID,
    val targetPlayerUUID: UUID,
    val targetServerName: String,
    val teleportType: TeleportType,
    val isSilentTeleport: Boolean,
    val createdAt: LocalDateTime,
)
