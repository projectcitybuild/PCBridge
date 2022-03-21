package com.projectcitybuild.core.infrastructure.network.pcb.requests

import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.AuthPlayerGroups
import com.projectcitybuild.entities.responses.AuthURL
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiInterface {

    /**
     * Begins the authentication flow by exchanging a player's UUID
     * for a URL that they can click and login with
     */
    @FormUrlEncoded
    @POST("auth/minecraft")
    suspend fun getVerificationUrl(
        @Field("minecraft_uuid") uuid: String
    ): ApiResponse<AuthURL>

    /**
     * Fetches the groups that the given UUID belongs to
     */
    @GET("auth/minecraft/{uuid}")
    suspend fun getUserGroups(
        @Path(value = "uuid") uuid: String
    ): ApiResponse<AuthPlayerGroups>
}
