package com.projectcitybuild.pcbridge.http.requests.pcb

import com.projectcitybuild.pcbridge.http.responses.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TelemetryAPIRequest {

    @FormUrlEncoded
    @POST("v2/minecraft/telemetry/seen")
    suspend fun seen(
        @Field(value = "uuid") playerUUID: String,
        @Field(value = "alias") playerName: String,
    ): ApiResponse<Unit>
}
