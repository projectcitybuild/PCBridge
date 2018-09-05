package com.projectcitybuild.api.interfaces

import com.projectcitybuild.entities.models.*
import com.projectcitybuild.entities.requests.GameBanRequest
import com.projectcitybuild.entities.requests.GameBanStatusRequest
import com.projectcitybuild.entities.requests.GameUnbanRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BanApiInterface {

    @POST("bans/store/ban")
    fun storeBan(@Body request: GameBanRequest) : Call<GameBan>

    @POST("bans/store/unban")
    fun storeUnban(@Body request: GameUnbanRequest) : Call<GameUnban>

    @POST("bans/status")
    fun requestStatus(@Body request: GameBanStatusRequest) : Call<GameBan?>

}