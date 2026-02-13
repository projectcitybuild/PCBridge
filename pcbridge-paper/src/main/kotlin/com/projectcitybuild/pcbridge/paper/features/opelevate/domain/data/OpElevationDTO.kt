package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data

import java.time.Duration
import java.time.Instant
import java.util.UUID

data class OpElevationDTO(
    val playerId: UUID,
    val endsAt: Instant,
) {
    fun remaining(now: Instant): Duration =
        Duration.between(now, endsAt)

    fun isExpired(now: Instant): Boolean =
        !remaining(now).isPositive

    fun isActive(now: Instant): Boolean =
        remaining(now).isPositive
}