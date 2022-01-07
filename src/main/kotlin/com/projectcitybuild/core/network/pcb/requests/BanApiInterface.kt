package com.projectcitybuild.core.network.pcb.requests

import com.projectcitybuild.entities.responses.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BanApiInterface {

    @FormUrlEncoded
    @POST("bans/store/ban")
    suspend fun storeBan(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerIdType: String,
            @Field("player_alias") playerAlias: String,
            @Field("staff_id") staffId: String?,
            @Field("staff_id_type") staffIdType: String,
            @Field("reason") reason: String? = null,
            @Field("expires_at") expiresAt: Long?,
            @Field("is_global_ban") isGlobalBan: Int
    ) : ApiResponse<GameBan>

    @FormUrlEncoded
    @POST("bans/store/unban")
    suspend fun storeUnban(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerIdType: String,
            @Field("staff_id") staffId: String?,
            @Field("staff_id_type") staffIdType: String
    ) : ApiResponse<GameUnban>

    @FormUrlEncoded
    @POST("bans/status")
    suspend fun requestStatus(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerType: String
    ) : ApiResponse<GameBan>

}