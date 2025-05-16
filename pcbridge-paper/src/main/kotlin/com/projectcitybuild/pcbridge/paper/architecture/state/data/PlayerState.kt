package com.projectcitybuild.pcbridge.paper.architecture.state.data

import com.projectcitybuild.pcbridge.http.pcb.models.Account
import com.projectcitybuild.pcbridge.http.pcb.models.Badge
import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.http.pcb.models.Player
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class PlayerState(
    @Serializable(with = LocalDateTimeSerializer::class)
    val connectedAt: LocalDateTime?,
    val account: Account? = null,
    val player: Player? = null,
    val groups: List<Group> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val afk: Boolean = false,
) {
    companion object {
        fun fromPlayerData(data: PlayerData, connectedAt: LocalDateTime) = PlayerState(
            connectedAt = connectedAt,
            account = data.account,
            player = data.player,
            groups = data.groups,
            badges = data.badges,
        )
    }
}