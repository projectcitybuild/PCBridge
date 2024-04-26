package com.projectcitybuild.pcbridge.http.requests

import com.projectcitybuild.pcbridge.http.responses.Account
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.parsing.ApiResponse
import com.projectcitybuild.pcbridge.http.responses.AuthURL
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import com.projectcitybuild.pcbridge.http.responses.PlayerWarning
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PCBRequest {
    /**
     * Begins the authentication flow by exchanging a player's UUID
     * for a URL that they can click and login with
     */
    @POST("auth/minecraft")
    @FormUrlEncoded
    suspend fun getVerificationUrl(
        @Field("minecraft_uuid") uuid: String
    ): ApiResponse<AuthURL>

    /**
     * Fetches the groups that the given UUID belongs to
     */
    @GET("auth/minecraft/{uuid}")
    suspend fun getUserGroups(
        @Path(value = "uuid") uuid: String
    ): ApiResponse<Account>

    /**
     * Fetches various data for the given uuid, such as ban status
     * and user groups. This is to make the login more efficient by
     * combining everything into one request
     */
    @GET("v2/minecraft/{uuid}/aggregate")
    suspend fun getAggregate(
        @Path(value = "uuid") uuid: String,
        @Query("ip") ip: String,
    ): ApiResponse<Aggregate>


    @GET("v2/minecraft/{uuid}/donation-tiers")
    suspend fun getDonationTier(
        @Path(value = "uuid") uuid: String
    ): ApiResponse<Array<DonationPerk>>


    @POST("v2/bans/ip/ban")
    @FormUrlEncoded
    suspend fun banIP(
        @Field("ip_address") ip: String,
        @Field("banner_player_id") bannerPlayerId: String?,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String,
    ): ApiResponse<IPBan>

    @POST("v2/bans/ip/unban")
    @FormUrlEncoded
    suspend fun unbanIP(
        @Field("ip_address") ip: String,
        @Field("unbanner_player_id") unbannerPlayerId: String?,
        @Field("unbanner_player_type") unbannerPlayerType: String = "minecraft_uuid",
        @Field("unbanner_player_alias") unbannerPlayerAlias: String,
    ): ApiResponse<IPBan>

    @POST("v2/bans/ip/status")
    @FormUrlEncoded
    suspend fun getIPStatus(
        @Field("ip_address") ip: String,
    ): ApiResponse<IPBan>


    @POST("v2/bans/player/ban")
    @FormUrlEncoded
    suspend fun banUUID(
        @Field("banned_player_id") bannedPlayerId: String,
        @Field("banned_player_type") bannedPlayerType: String = "minecraft_uuid",
        @Field("banned_player_alias") bannedPlayerAlias: String,
        @Field("banner_player_id") bannerPlayerId: String?,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String? = null,
        @Field("expires_at") expiresAt: Long?,
    ): ApiResponse<PlayerBan>


    @POST("v2/bans/player/unban")
    @FormUrlEncoded
    suspend fun unbanUUID(
        @Field("banned_player_id") bannedPlayerId: String,
        @Field("banned_player_type") bannedPlayerType: String = "minecraft_uuid",
        @Field("unbanner_player_id") unbannerPlayerId: String?,
        @Field("unbanner_player_type") unbannerPlayerType: String = "minecraft_uuid"
    ): ApiResponse<PlayerBan>

    @POST("v2/bans/player/status")
    @FormUrlEncoded
    suspend fun getUuidBanStatus(
        @Field("player_id") playerId: String,
        @Field("player_type") playerType: String = "minecraft_uuid"
    ): ApiResponse<PlayerBan>

    @POST("v2/bans/player/all")
    @FormUrlEncoded
    suspend fun getUUIDBans(
        @Field("player_id") playerId: String,
        @Field("player_id_type") playerType: String = "minecraft_uuid"
    ): ApiResponse<List<PlayerBan>>

    @POST("v2/bans/player/ban")
    @FormUrlEncoded
    suspend fun convertToPermanentBan(
        @Field("ban_id") bannedPlayerId: String,
        @Field("banner_player_id") bannerPlayerId: String,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String? = null,
    ): ApiResponse<PlayerBan>


    @GET("v2/warnings")
    suspend fun getWarnings(
        @Query("player_id") playerId: String,
        @Query("player_type") playerType: String = "minecraft_uuid",
        @Query("player_alias") playerAlias: String,
    ): ApiResponse<List<PlayerWarning>>

    @POST("v2/warnings")
    @FormUrlEncoded
    suspend fun createWarning(
        @Field("warned_player_id") warnedPlayerId: String,
        @Field("warned_player_type") warnedPlayerType: String = "minecraft_uuid",
        @Field("warned_player_alias") warnedPlayerAlias: String,
        @Field("warner_player_id") warnerPlayerId: String,
        @Field("warner_player_type") warnerPlayerType: String = "minecraft_uuid",
        @Field("warner_player_alias") warnerPlayerAlias: String,
        @Field("reason") reason: String,
        @Field("weight") weight: Int = 1,
    ): ApiResponse<PlayerWarning>

    @POST("v2/warnings/acknowledge")
    @FormUrlEncoded
    suspend fun acknowledgeWarning(
        @Field("warning_id") warningId: Int,
    ): ApiResponse<PlayerWarning>


    @POST("v2/minecraft/telemetry/seen")
    @FormUrlEncoded
    suspend fun telemetrySeen(
        @Field(value = "uuid") playerUUID: String,
        @Field(value = "alias") playerName: String,
    ): ApiResponse<Unit>
}
