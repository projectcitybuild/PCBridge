package com.projectcitybuild.pcbridge.paper.architecture.state.data

import com.projectcitybuild.pcbridge.http.pcb.models.Account
import com.projectcitybuild.pcbridge.http.pcb.models.Badge
import com.projectcitybuild.pcbridge.http.pcb.models.Role
import com.projectcitybuild.pcbridge.http.pcb.models.Player
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.features.pim.domain.data.OpElevation
import com.projectcitybuild.pcbridge.paper.features.pim.domain.data.toDomain
import java.time.Duration
import java.time.LocalDateTime

data class PlayerSession(
    val synced: PlayerSyncedState,
    val connectedAt: LocalDateTime,
    val afk: Boolean = false,
) {
    val syncedValue: PlayerSyncedState.Valid?
        get() = synced as? PlayerSyncedState.Valid

    fun sessionSeconds(time: LocalizedTime): Long {
        val now = time.now()
        val diff = Duration.between(connectedAt, now)
        return diff.toSeconds()
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