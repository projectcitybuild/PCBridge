package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class IPBan(
    @SerializedName("id") val id: Int = Math.random().toInt(),
    @SerializedName("banner_player_id") val bannerPlayerId: String = UUID.randomUUID().toString(),
    @SerializedName("reason") val reason: String = "reason",
    @SerializedName("created_at") val createdAt: Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
    @SerializedName("updated_at") val updatedAt: Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
    @SerializedName("unbanned_at") val unbannedAt: Long? = null,
    @SerializedName("unbanner_player_id") val unbannerPlayerId: String? = null,
    @SerializedName("unban_type") val unbanType: String? = null,
) {
    val isActive: Boolean
        get() = unbannedAt == null
}
