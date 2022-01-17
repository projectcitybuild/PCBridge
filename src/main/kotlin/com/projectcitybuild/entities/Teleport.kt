package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class Teleport(
    val playerUUID: UUID,
    val targetPlayerUUID: UUID,
    val targetServerName: String,
    val teleportType: TeleportType,
    val createdAt: LocalDateTime,
)

enum class TeleportType {
    TP,
    SUMMON,
}
