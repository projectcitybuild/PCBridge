package com.projectcitybuild.pcbridge.paper.features.opelevate.domain.data

import com.projectcitybuild.pcbridge.http.pcb.models.HttpOpElevation
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

data class OpElevation(
    val playerId: Long,
    val reason: String,
    val startedAt: Instant,
    val endsAt: Instant,
) {
    fun remaining(now: Instant): Duration
        = Duration.between(now, endsAt)

    fun state(now: Instant): State
        = if (remaining(now).isPositive) State.ACTIVE
        else State.EXPIRED

    fun transition(now: Instant): Transition
        = when (state(now)) {
            State.ACTIVE -> Transition.Grant(remaining(now))
            State.EXPIRED -> Transition.Expire
        }

    enum class State {
        ACTIVE,
        EXPIRED,
    }

    sealed interface Transition {
        data class Grant(val remaining: Duration) : Transition
        data object Expire : Transition
    }
}

fun HttpOpElevation.toDomain(
    zone: ZoneId = ZoneOffset.UTC
) = OpElevation(
    playerId = playerId,
    reason = reason,
    startedAt = startedAt.atZone(zone).toInstant(),
    endsAt = endedAt.atZone(zone).toInstant(),
)