package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class PlayerBan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("banned_player_id")
    val bannedPlayerId: Int,

    @SerializedName("banned_player_alias")
    val bannedPlayerAlias: String,

    @SerializedName("banner_player_id")
    val bannerPlayerId: Int? = null,

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
