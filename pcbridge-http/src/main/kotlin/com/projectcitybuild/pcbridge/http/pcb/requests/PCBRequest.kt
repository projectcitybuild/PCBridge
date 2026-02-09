package com.projectcitybuild.pcbridge.http.pcb.requests

import com.projectcitybuild.pcbridge.http.pcb.models.Authorization
import com.projectcitybuild.pcbridge.http.pcb.models.Build
import com.projectcitybuild.pcbridge.http.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.http.pcb.models.Home
import com.projectcitybuild.pcbridge.http.pcb.models.HomeLimit
import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal fun Retrofit.pcb() = create(PCBRequest::class.java)

internal interface PCBRequest {
    @POST("v3/server/connection/authorize")
    @FormUrlEncoded
    suspend fun connectionAuth(
        @Field("uuid") uuid: String,
        @Field("ip") ip: String?,
    ): Authorization

    @POST("v3/server/connection/end")
    @FormUrlEncoded
    suspend fun connectionEnd(
        @Field("uuid") uuid: String,
        @Field("session_seconds") sessionSeconds: Long,
    )

    @GET("v3/server/config")
    suspend fun getConfig(): RemoteConfigVersion

    @POST("v3/players/{uuid}/stats")
    @FormUrlEncoded
    suspend fun incrementStats(
        @Path("uuid") uuid: String,
        @Field("afk_time") afkTime: Long? = null,
        @Field("blocks_placed") blocksPlaced: Long? = null,
        @Field("blocks_destroyed") blocksDestroyed: Long? = null,
        @Field("blocks_travelled") blocksTravelled: Long? = null,
    )

    /**
     * Begins registration of a PCB account linked to the current
     * Minecraft player
     */
    @POST("v3/players/{uuid}/register")
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
    @PUT("v3/players/{uuid}/register")
    @FormUrlEncoded
    suspend fun verifyRegisterCode(
        @Path(value = "uuid") uuid: String,
        @Field(value = "code") code: String,
    )

    @GET("v3/warps")
    suspend fun getWarps(
        @Query(value = "page") page: Int,
        @Query(value = "page_size") size: Int,
    ): PaginatedList<Warp>

    @GET("v3/warps/all")
    suspend fun getAllWarps(): List<Warp>

    @GET("v3/warps/{id}")
    suspend fun getWarp(
        @Path(value = "id") id: Int,
    ): Warp?

    @POST("v3/warps")
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

    @PUT("v3/warps/{id}")
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

    @DELETE("v3/warps/{id}")
    suspend fun deleteWarp(
        @Path(value = "id") id: Int,
    )

    @GET("v3/warps/names")
    suspend fun getWarpNames(): List<NamedResource>

    @GET("v3/builds/names")
    suspend fun getBuildNames(): List<NamedResource>

    @GET("v3/builds")
    suspend fun getBuilds(
        @Query(value = "page") page: Int,
        @Query(value = "page_size") size: Int,
    ): PaginatedList<Build>

    @GET("v3/builds/{id}")
    suspend fun getBuild(
        @Path(value = "id") id: Int,
    ): Build?

    @POST("v3/builds")
    @FormUrlEncoded
    suspend fun createBuild(
        @Field(value = "player_uuid") playerUUID: String,
        @Field(value = "alias") playerAlias: String,
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Build

    @PUT("v3/builds/{id}")
    @FormUrlEncoded
    suspend fun updateBuild(
        @Path(value = "id") id: Int,
        @Field(value = "player_uuid") playerUUID: String,
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Build

    @PATCH("v3/builds/{id}/set")
    @FormUrlEncoded
    suspend fun setBuildField(
        @Path(value = "id") id: Int,
        @Field(value = "player_uuid") playerUUID: String,
        @Field(value = "name") name: String?,
        @Field(value = "description") description: String?,
        @Field(value = "lore") lore: String?,
    ): Build

    @DELETE("v3/builds/{id}")
    suspend fun deleteBuild(
        @Path(value = "id") id: Int,
        @Query(value = "player_uuid") playerUUID: String,
    )

    @POST("v3/builds/{id}/vote")
    @FormUrlEncoded
    suspend fun buildVote(
        @Path(value = "id") id: Int,
        @Field(value = "player_uuid") playerUUID: String,
    ): Build

    @DELETE("v3/builds/{id}/vote")
    suspend fun buildUnvote(
        @Path(value = "id") id: Int,
        @Query(value = "player_uuid") playerUUID: String,
    ): Build

    @GET("v3/players/{player_uuid}/homes")
    suspend fun getHomes(
        @Path(value = "player_uuid") playerUUID: String,
        @Query(value = "page") page: Int,
        @Query(value = "page_size") size: Int,
    ): PaginatedList<Home>

    @POST("v3/players/{player_uuid}/homes")
    @FormUrlEncoded
    suspend fun createHome(
        @Path(value = "player_uuid") playerUUID: String,
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Home

    @PUT("v3/players/{player_uuid}/homes/{id}")
    @FormUrlEncoded
    suspend fun updateHome(
        @Path(value = "player_uuid") playerUUID: String,
        @Path(value = "id") id: Int,
        @Field(value = "name") name: String,
        @Field(value = "world") world: String,
        @Field(value = "x") x: Double,
        @Field(value = "y") y: Double,
        @Field(value = "z") z: Double,
        @Field(value = "pitch") pitch: Float,
        @Field(value = "yaw") yaw: Float,
    ): Home

    @DELETE("v3/players/{player_uuid}/homes/{id}")
    suspend fun deleteHome(
        @Path(value = "player_uuid") playerUUID: String,
        @Path(value = "id") id: Int,
    )

    @GET("v3/players/{player_uuid}/homes/{id}")
    suspend fun getHome(
        @Path(value = "player_uuid") playerUUID: String,
        @Path(value = "id") id: Int,
    ): Home?

    @GET("v3/players/{player_uuid}/homes/names")
    suspend fun getHomeNames(
        @Path(value = "player_uuid") playerUUID: String,
    ): List<NamedResource>

    @GET("v3/players/{player_uuid}/homes/limit")
    suspend fun getHomeLimit(
        @Path(value = "player_uuid") playerUUID: String,
    ): HomeLimit

    @GET("v3/players/{player_uuid}/bans")
    suspend fun getPlayerBans(
        @Path(value = "player_uuid") playerUUID: String,
        @Query(value = "only_active") onlyActiveBans: Boolean? = null,
    ): List<PlayerBan>

    @POST("v3/bans/uuid")
    @FormUrlEncoded
    suspend fun createUuidBan(
        @Field(value = "banned_uuid") bannedUUID: String,
        @Field(value = "banned_alias") bannedAlias: String,
        @Field(value = "banner_uuid") bannerUUID: String?,
        @Field(value = "banner_alias") bannerAlias: String?,
        @Field(value = "reason") reason: String,
        @Field(value = "additional_info") additionalInfo: String?,
    ): PlayerBan
}
