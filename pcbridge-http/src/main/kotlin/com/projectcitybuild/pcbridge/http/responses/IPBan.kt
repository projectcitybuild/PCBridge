package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class IPBan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("banner_player_id")
    val bannerPlayerId: Int,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @SerializedName("updated_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,

    @SerializedName("unbanned_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val unbannedAt: LocalDateTime? = null,

    @SerializedName("unbanner_player_id")
    val unbannerPlayerId: String? = null,

    @SerializedName("unban_type")
    val unbanType: String? = null,
) {
    val isActive: Boolean
        get() = unbannedAt == null
}
