package com.projectcitybuild.pcbridge.http.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class PlayerBan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("banned_alias_at_time")
    val bannedPlayerAlias: String? = null,

    @SerializedName("banned_player")
    val bannedPlayer: Player? = null,

    @SerializedName("banner_player")
    val bannerPlayer: Player? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @SerializedName("updated_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,

    @SerializedName("expires_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime? = null,

    @SerializedName("unbanned_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    var unbannedAt: LocalDateTime? = null,

    @SerializedName("unbanner_player_id")
    var unbannerPlayerId: String? = null,

    @SerializedName("unban_type")
    var unbanType: String? = null,
) {
    val isActive: Boolean
        get() = unbannedAt == null
}
