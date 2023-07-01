package com.projectcitybuild.entities.requests.pcb

import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.IPBan
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IPBanAPIRequest {

    @FormUrlEncoded
    @POST("v2/bans/ip/ban")
    suspend fun ban(
        @Field("ip_address") ip: String,
        @Field("banner_player_id") bannerPlayerId: String?,
        @Field("banner_player_type") bannerPlayerType: String = "minecraft_uuid",
        @Field("banner_player_alias") bannerPlayerAlias: String,
        @Field("reason") reason: String,
    ): ApiResponse<IPBan>

    @FormUrlEncoded
    @POST("v2/bans/ip/unban")
    suspend fun unban(
        @Field("ip_address") ip: String,
        @Field("unbanner_player_id") unbannerPlayerId: String?,
        @Field("unbanner_player_type") unbannerPlayerType: String = "minecraft_uuid",
        @Field("unbanner_player_alias") unbannerPlayerAlias: String,
    ): ApiResponse<IPBan>

    @FormUrlEncoded
    @POST("v2/bans/ip/status")
    suspend fun status(
        @Field("ip_address") ip: String,
    ): ApiResponse<IPBan>
}
