package com.projectcitybuild.pcbridge.http.requests

import com.projectcitybuild.pcbridge.http.parsing.ApiResponse
import com.projectcitybuild.pcbridge.http.responses.PlayerData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PCBRequest {
    /**
     * Fetches all data for the given UUID, such as ban status,
     * user groups and badges
     */
    @GET("v2/minecraft/player/{uuid}")
    suspend fun getPlayer(
        @Path(value = "uuid") uuid: String,
        @Query("ip") ip: String,
    ): PlayerData

    /**
     * Begins registration of a PCB account linked to the current
     * Minecraft player
     */
    @POST("v2/minecraft/player/{uuid}/register")
    @FormUrlEncoded
    suspend fun sendRegisterCode(
        @Path(value = "uuid") uuid: String,
        @Field(value = "minecraft_alias") playerAlias: String,
        @Field(value = "email") email: String,
    )

    /**
     * Finishes registration by verifying the code sent to them
     * over email
     */
    @PUT("v2/minecraft/player/{uuid}/register")
    @FormUrlEncoded
    suspend fun verifyRegisterCode(
        @Path(value = "uuid") uuid: String,
        @Field(value = "code") code: String,
    )

    /**
     * Updates the last seen date of the player
     */
    @POST("v2/minecraft/telemetry/seen")
    @FormUrlEncoded
    suspend fun telemetrySeen(
        @Field(value = "uuid") playerUUID: String,
        @Field(value = "alias") playerName: String,
    ): ApiResponse<Unit>
}
