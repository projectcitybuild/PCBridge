package com.projectcitybuild.pcbridge.http.requests

import com.projectcitybuild.pcbridge.http.models.PlayerData
import com.projectcitybuild.pcbridge.http.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.models.Warp
import retrofit2.Retrofit
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal fun Retrofit.pcb() = create(PCBRequest::class.java)

internal interface PCBRequest {
    /**
     * Fetches all data for the given UUID, such as ban status,
     * user groups and badges
     */
    @GET("v2/minecraft/player/{uuid}")
    suspend fun getPlayer(
        @Path(value = "uuid") uuid: String,
        @Query("ip") ip: String?,
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
    )

    /**
     * Fetches the latest Minecraft config
     */
    @GET("v2/minecraft/config")
    suspend fun getConfig(): RemoteConfigVersion

    @GET("v2/minecraft/warp")
    suspend fun getWarps(): List<Warp>

    @POST("v2/minecraft/warp")
    @FormUrlEncoded
    suspend fun createWarp(
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Warp

    @PUT("v2/minecraft/warp/{id}")
    @FormUrlEncoded
    suspend fun updateWarp(
        @Path(value = "id") id: Int,
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Warp

    @DELETE("v2/minecraft/warp/{id}")
    suspend fun deleteWarp(
        @Path(value = "id") id: Int,
    )
}
