package com.projectcitybuild.core.api.interfaces

import com.projectcitybuild.core.entities.models.*
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BanApiInterface {

    @FormUrlEncoded
    @POST("bans/store/ban")
    fun storeBan(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerIdType: String,
            @Field("player_alias") playerAlias: String,
            @Field("staff_id") staffId: String?,
            @Field("staff_id_type") staffIdType: String,
            @Field("reason") reason: String?,
            @Field("expires_at") expiresAt: Long?,
            @Field("is_global_ban") isGlobalBan: Int
    ) : Call<ApiResponse<GameBan>>

    @FormUrlEncoded
    @POST("bans/store/unban")
    fun storeUnban(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerIdType: String,
            @Field("staff_id") staffId: String?,
            @Field("staff_id_type") staffIdType: String
    ) : Call<ApiResponse<GameUnban>>

    @FormUrlEncoded
    @POST("bans/status")
    fun requestStatus(
            @Field("player_id") playerId: String,
            @Field("player_id_type") playerType: String
    ) : Call<ApiResponse<GameBan>>

}