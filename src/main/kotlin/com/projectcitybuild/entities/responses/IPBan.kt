package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class IPBan(
    @SerializedName("id") val id: Int,
    @SerializedName("banner_player_id") val bannerPlayerId: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("unbanned_at") val unbannedAt: Long?,
    @SerializedName("unbanner_player_id") val unbannerPlayerId: String?,
    @SerializedName("unban_type") val unbanType: String?,
)
