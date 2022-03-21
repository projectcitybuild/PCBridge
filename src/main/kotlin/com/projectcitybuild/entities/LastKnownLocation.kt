package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.UUID

data class LastKnownLocation(
    val playerUUID: UUID,
    val location: CrossServerLocation,
    val createdAt: LocalDateTime,
)
