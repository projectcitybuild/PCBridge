package com.projectcitybuild.core.network.pcb.requests

import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.AuthPlayerGroups
import com.projectcitybuild.core.entities.models.AuthURL
import retrofit2.http.*

interface AuthApiInterface {

    /**
     * Begins the authentication flow by exchanging a player's UUID
     * for a URL that they can click and login with
     */
    @FormUrlEncoded
    @POST("auth/minecraft")
    suspend fun getVerificationUrl(
            @Field("minecraft_uuid") uuid: String
    ) : ApiResponse<AuthURL>

    /**
     * Fetches the groups that the given UUID belongs to
     */
    @GET("auth/minecraft/{uuid}")
    suspend fun getUserGroups(
            @Path(value = "uuid") uuid: String
    ) : ApiResponse<AuthPlayerGroups>

}