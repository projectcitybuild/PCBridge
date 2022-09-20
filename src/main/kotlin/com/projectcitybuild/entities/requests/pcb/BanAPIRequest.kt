package com.projectcitybuild.entities.requests.pcb

import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.PlayerBan
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BanAPIRequest {

    @FormUrlEncoded
    @POST("v2/bans/ban")
    suspend fun ban(
        @Field("banned_player_id") bannedPlayerId: String,
        @Field("banned_player_type") bannedPlayerType: String = "minecraft_uuid",
        @Field("banned_player_alias") bannedPlayerAlias: String,
        @Field("banner_player_id") bannerPlayerId: String?,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String? = null,
        @Field("expires_at") expiresAt: Long?,
    ): ApiResponse<PlayerBan>

    @FormUrlEncoded
    @POST("v2/bans/unban")
    suspend fun unban(
        @Field("banned_player_id") bannedPlayerId: String,
        @Field("banned_player_type") bannedPlayerType: String = "minecraft_uuid",
        @Field("unbanner_player_id") unbannerPlayerId: String?,
        @Field("unbanner_player_type") unbannerPlayerType: String = "minecraft_uuid"
    ): ApiResponse<PlayerBan>

    @FormUrlEncoded
    @POST("v2/bans/status")
    suspend fun status(
        @Field("player_id") playerId: String,
        @Field("player_type") playerType: String = "minecraft_uuid"
    ): ApiResponse<PlayerBan>

    @FormUrlEncoded
    @POST("v2/bans/all")
    suspend fun all(
        @Field("player_id") playerId: String,
        @Field("player_id_type") playerType: String = "minecraft_uuid"
    ): ApiResponse<List<PlayerBan>>

    @FormUrlEncoded
    @POST("v2/bans/ban")
    suspend fun convertToPermanentBan(
        @Field("ban_id") bannedPlayerId: String,
        @Field("banner_player_id") bannerPlayerId: String,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String? = null,
    ): ApiResponse<PlayerBan>
}
