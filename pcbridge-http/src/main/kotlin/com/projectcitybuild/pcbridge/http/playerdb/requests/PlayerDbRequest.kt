package com.projectcitybuild.pcbridge.http.playerdb.requests

import com.projectcitybuild.pcbridge.http.playerdb.models.PlayerDbMinecraftPlayer
import com.projectcitybuild.pcbridge.http.playerdb.models.PlayerDbResponse
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

internal fun Retrofit.playerDb() = create(PlayerDbRequest::class.java)

internal interface PlayerDbRequest {
    @GET("player/minecraft/{name}")
    suspend fun getPlayer(
        @Path(value = "name") name: String,
    ): PlayerDbResponse<PlayerDbMinecraftPlayer>
}
