package com.projectcitybuild.entities

import java.sql.Date
import java.util.*

data class TeleportRecord(
    val id: Int,
    val playerUUID: UUID,
    val teleportReason: TeleportReason,
    val location: CrossServerLocation,
    val createdAt: Date,
)
