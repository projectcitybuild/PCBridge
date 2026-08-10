package com.projectcitybuild.pcbridge.core.store.data

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.store.data.PlayerSession.OpElevation
import com.projectcitybuild.pcbridge.http.pcb.models.Account
import com.projectcitybuild.pcbridge.http.pcb.models.Badge
import com.projectcitybuild.pcbridge.http.pcb.models.HttpOpElevation
import com.projectcitybuild.pcbridge.http.pcb.models.Role
import com.projectcitybuild.pcbridge.http.pcb.models.Player
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

// TODO: allow features to define and watch their own slice
data class PlayerSession(
    val synced: PlayerSyncedState,
    val connectedAt: LocalDateTime,
    val afkStartedAt: Instant? = null,
) {
    val syncedValue: PlayerSyncedState.Valid?
        get() = synced as? PlayerSyncedState.Valid

    val afk: Boolean
        get() = afkStartedAt != null

    fun sessionSeconds(time: LocalizedTime): Long {
        val now = time.now()
        val diff = Duration.between(connectedAt, now)
        return diff.toSeconds()
    }

    // TODO: remove this duplicate
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

    companion object {
        fun fromPlayerData(
            data: PlayerData?,
            connectedAt: LocalDateTime,
        ) = PlayerSession(
            connectedAt = connectedAt,
            synced = if (data == null) PlayerSyncedState.Unavailable
                else PlayerSyncedState.Valid(
                    account = data.account,
                    player = data.player,
                    roles = data.roles,
                    badges = data.badges,
                    opElevation = data.elevation?.toDomain(),
                ),
        )
    }
}

sealed class PlayerSyncedState {
    object Unavailable: PlayerSyncedState()

    data class Valid(
        val account: Account? = null,
        val player: Player? = null,
        val roles: List<Role> = emptyList(),
        val badges: List<Badge> = emptyList(),
        val opElevation: OpElevation? = null,
    ): PlayerSyncedState()
}

// TODO: remove this duplicate
private fun HttpOpElevation.toDomain() = OpElevation(
    playerId = playerId,
    reason = reason,
    startedAt = startedAt,
    endsAt = endedAt,
)