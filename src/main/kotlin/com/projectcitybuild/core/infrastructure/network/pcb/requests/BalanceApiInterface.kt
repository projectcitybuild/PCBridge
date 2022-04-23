package com.projectcitybuild.core.infrastructure.network.pcb.requests

import com.projectcitybuild.entities.responses.AccountBalance
import com.projectcitybuild.entities.responses.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BalanceApiInterface {

    @GET("v2/minecraft/{uuid}/balance")
    suspend fun get(
        @Path(value = "uuid") uuid: String,
    ): ApiResponse<AccountBalance>

    @FormUrlEncoded
    @POST("v2/minecraft/{uuid}/balance/deduct")
    suspend fun deduct(
        @Path(value = "uuid") uuid: String,
        @Field("amount") amount: Int,
        @Field("reason") reason: String,
    ): ApiResponse<Unit>
}
