package com.projectcitybuild.api.interfaces

import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.LoginResult
import com.projectcitybuild.entities.models.VerificationUrl
import retrofit2.Call
import retrofit2.http.*

interface RankApiInterface {

    @FormUrlEncoded
    @POST("auth/minecraft")
    fun fetchVerificationURL(
            @Field("minecraft_uuid") uuid: String
    ) : Call<ApiResponse<VerificationUrl>>

    @GET("auth/minecraft/{minecraft_uuid}")
    fun fetchGroups(
            @Path(value = "minecraft_uuid", encoded = true) uuid: String
    ) : Call<ApiResponse<VerificationUrl>>

}