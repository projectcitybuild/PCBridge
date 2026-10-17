package com.projectcitybuild.pcbridge.features.teleportation.domain.data

import java.time.Instant
import java.util.UUID

data class TeleportRequest(
    val requesterId: UUID,
    val requesterName: String,
    val targetId: UUID,
    val targetName: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val silent: Boolean,
    val direction: Direction,
) {
    enum class Direction {
        TO,
        HERE,
    }
}