package com.projectcitybuild.pcbridge.paper.features.pim.domain.data

import com.projectcitybuild.pcbridge.http.pcb.models.HttpOpElevation
import java.time.Duration
import java.time.Instant

data class OpElevation(
    val playerId: Long,
    val reason: String,
    val startedAt: Instant,
    val endsAt: Instant,
) {
    fun remainingAt(now: Instant): Duration? {
        val remaining = Duration.between(now, endsAt)
        return remaining.takeIf { it.isPositive }
    }

    fun isActiveAt(now: Instant): Boolean =
        remainingAt(now) != null
}

fun HttpOpElevation.toDomain() = OpElevation(
    playerId = playerId,
    reason = reason,
    startedAt = startedAt,
    endsAt = endedAt,
)