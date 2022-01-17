package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class Teleport(
    val playerUUID: UUID,
    val targetPlayerUUID: UUID,
    val targetServerName: String,
    val createdAt: LocalDateTime,
)
