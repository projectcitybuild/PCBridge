package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class GameBan(
    @SerializedName("game_ban_id") val id: Int,
    @SerializedName("server_id") val serverId: Int,
    @SerializedName("banned_player_id") val playerId: String,
    @SerializedName("banned_player_type") val playerType: String,
    @SerializedName("banned_alias_at_time") val playerAlias: String,
    @SerializedName("staff_player_id") val staffId: String?,
    @SerializedName("staff_player_type") val staffType: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_global_ban") val isGlobalBan: Boolean,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("expires_at") val expiresAt: Long?
)
