package com.projectcitybuild.core.infrastructure.network.mojang.requests

import com.projectcitybuild.entities.responses.MojangPlayer
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MojangApiInterface {

    @GET("users/profiles/minecraft/{username}")
    suspend fun getMojangPlayer(
            @Path("username") playerName: String, @Query("at") timestamp: Long? = null
    ) : MojangPlayer?
}