package com.projectcitybuild.entities.requests.pcb

import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.PlayerWarning
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WarningAPIRequest {

    @GET("v2/warnings")
    suspend fun get(
        @Query("player_id") bannedPlayerId: String,
        @Query("player_type") bannedPlayerType: String = "minecraft_uuid",
        @Query("player_alias") bannedPlayerAlias: String,
    ): ApiResponse<List<PlayerWarning>>

    @FormUrlEncoded
    @POST("v2/warnings")
    suspend fun create(
        @Field("warned_player_id") warnedPlayerId: String,
        @Field("warned_player_type") warnedPlayerType: String = "minecraft_uuid",
        @Field("warned_player_alias") warnedPlayerAlias: String,
        @Field("warner_player_id") warnerPlayerId: String,
        @Field("warner_player_type") warnerPlayerType: String = "minecraft_uuid",
        @Field("warner_player_alias") warnerPlayerAlias: String,
        @Field("reason") reason: String,
        @Field("weight") weight: Int = 1,
    ): ApiResponse<PlayerWarning>

    @FormUrlEncoded
    @POST("v2/warnings/acknowledge")
    suspend fun acknowledge(
        @Field("warning_id") warningId: Int,
    ): ApiResponse<PlayerWarning>
}
