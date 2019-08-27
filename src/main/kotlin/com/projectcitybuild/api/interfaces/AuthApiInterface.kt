package com.projectcitybuild.api.interfaces

import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.AuthPlayerGroups
import com.projectcitybuild.entities.models.AuthURL
import com.projectcitybuild.entities.models.LoginResult
import retrofit2.Call
import retrofit2.http.*

interface AuthApiInterface {

    /**
     * Begins the authentication flow by exchanging a player's UUID
     * for a URL that they can click and login with
     */
    @FormUrlEncoded
    @POST("auth/minecraft")
    fun getVerificationUrl(
            @Field("minecraft_uuid") uuid: String
    ) : Call<ApiResponse<AuthURL>>

    /**
     * Fetches the groups that the given UUID belongs to
     */
    @FormUrlEncoded
    @GET("auth/minecraft/{uuid}")
    fun getUserGroups(
            @Path(value = "uuid", encoded = false) uuid: String
    ) : Call<ApiResponse<AuthPlayerGroups>>

}