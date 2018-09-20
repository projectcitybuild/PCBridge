package com.projectcitybuild.api.interfaces

import com.projectcitybuild.entities.models.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MojangApiInterface {

    @GET("users/profiles/minecraft/{username}")
    fun getMojangPlayer(
            @Path("username") playerName: String,
            @Query("at") timestamp: Long? = null
    ) : Call<MojangPlayer>

}