package com.projectcitybuild.entities

import java.sql.Date
import java.util.*

data class TeleportRecord(
    val id: Int,
    val playerUUID: UUID,
    val teleportReason: TeleportReason,
    val serverName: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val createdAt: Date,
)

enum class TeleportReason {
    TP_TO,
    TP_SUMMONED,
    WARPED,
}