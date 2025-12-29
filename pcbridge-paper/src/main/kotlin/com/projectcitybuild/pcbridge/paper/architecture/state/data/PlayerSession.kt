package com.projectcitybuild.pcbridge.paper.architecture.state.data

import com.projectcitybuild.pcbridge.http.pcb.models.Account
import com.projectcitybuild.pcbridge.http.pcb.models.Badge
import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.http.pcb.models.Player
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
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
                    groups = data.groups,
                    badges = data.badges,
                ),
        )
    }
}

sealed class PlayerSyncedState {
    object Unavailable: PlayerSyncedState()

    data class Valid(
        val account: Account? = null,
        val player: Player? = null,
        val groups: List<Group> = emptyList(),
        val badges: List<Badge> = emptyList(),
    ): PlayerSyncedState()
}