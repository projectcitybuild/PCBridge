package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class TeleportRecord(
    val id: Int,
    val playerUUID: UUID,
    val teleportReason: TeleportReason,
    val location: CrossServerLocation,
    val canGoBack: Boolean,
    val createdAt: LocalDateTime,
)
