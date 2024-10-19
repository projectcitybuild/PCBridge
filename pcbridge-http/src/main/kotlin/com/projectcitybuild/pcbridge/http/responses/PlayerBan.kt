package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class PlayerBan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("server_id")
    val serverId: Int,

    @SerializedName("banned_player_id")
    val bannedPlayerId: String,

    @SerializedName("banned_player_alias")
    val bannedPlayerAlias: String,

    @SerializedName("banner_player_id")
    val bannerPlayerId: String?,

    @SerializedName("reason")
    val reason: String?,

    @SerializedName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @SerializedName("updated_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,

    @SerializedName("expires_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime?,

    @SerializedName("unbanned_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    var unbannedAt: LocalDateTime?,

    @SerializedName("unbanner_player_id")
    var unbannerPlayerId: String?,

    @SerializedName("unban_type")
    var unbanType: String?,
) {
    val isActive: Boolean
        get() = unbannedAt == null
}
