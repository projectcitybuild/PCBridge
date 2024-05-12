package com.projectcitybuild.pcbridge.http.requests

import com.projectcitybuild.pcbridge.http.responses.MojangPlayer
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface MojangRequest {
    @GET("users/profiles/minecraft/{username}")
    suspend fun getMojangPlayer(
        @Path("username") playerName: String,
        @Query("at") timestamp: Long? = null,
    ): MojangPlayer?
}
