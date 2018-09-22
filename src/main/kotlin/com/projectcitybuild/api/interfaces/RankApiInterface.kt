package com.projectcitybuild.api.interfaces

import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.LoginResult
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RankApiInterface {

    @FormUrlEncoded
    @POST("minecraft/authenticate")
    fun login(
            @Field("email") email: String,
            @Field("password") password: String
    ) : Call<ApiResponse<LoginResult>>

}