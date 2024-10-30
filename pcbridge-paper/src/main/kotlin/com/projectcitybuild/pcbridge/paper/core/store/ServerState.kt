package com.projectcitybuild.pcbridge.paper.core.store

import com.projectcitybuild.pcbridge.http.models.Account
import com.projectcitybuild.pcbridge.http.models.Badge
import com.projectcitybuild.pcbridge.http.models.DonationPerk
import com.projectcitybuild.pcbridge.http.models.Player
import com.projectcitybuild.pcbridge.http.models.PlayerData
import com.projectcitybuild.pcbridge.http.serialization.serializable.LocalDateTimeSerializer
import com.projectcitybuild.pcbridge.http.serialization.serializable.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents in-memory, global state for the plugin.
 *
 * This state is persisted to storage on plugin shutdown,
 * and restored upon boot.
 */
@Serializable(with = UUIDSerializer::class)
data class ServerState(
    /**
     * Individual state for each online player
     */
    val players: MutableMap<UUID, PlayerState>,
    /**
     * Index of the last announcement broadcast to players on the server.
     *
     * This is used to enumerate over the announcement list, and remember
     * which one was last broadcast when reloading
     */
    val lastBroadcastIndex: Int,
)

@Serializable
data class PlayerState(
    @Serializable(with = LocalDateTimeSerializer::class)
    val connectedAt: LocalDateTime?,
    val account: Account? = null,
    val player: Player? = null,
    val badges: List<Badge> = emptyList(),
    val donationPerks: List<DonationPerk> = emptyList(),
) {
    companion object {
        fun fromPlayerData(data: PlayerData, connectedAt: LocalDateTime) = PlayerState(
            connectedAt = connectedAt,
            account = data.account,
            player = data.player,
            badges = data.badges,
            donationPerks = data.donationPerks,
        )
    }
}
