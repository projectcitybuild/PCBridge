package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class PlayerBan(
    @SerializedName("id")
    val id: Int = Math.random().toInt(),
    @SerializedName("server_id")
    val serverId: Int = Math.random().toInt(),
    @SerializedName("banned_player_id")
    val bannedPlayerId: String = UUID.randomUUID().toString(),
    @SerializedName("banned_player_alias")
    val bannedPlayerAlias: String = "name",
    @SerializedName("banner_player_id")
    val bannerPlayerId: String? = UUID.randomUUID().toString(),
    @SerializedName("reason")
    val reason: String? = "reason",
    @SerializedName("created_at")
    val createdAt: Long =
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond(),
    @SerializedName("updated_at")
    val updatedAt: Long =
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond(),
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    @SerializedName("unbanned_at")
    var unbannedAt: Long? = null,
    @SerializedName("unbanner_player_id")
    var unbannerPlayerId: String? = null,
    @SerializedName("unban_type")
    var unbanType: String? = null,
) {
    val isActive: Boolean
        get() = unbannedAt == null
}
