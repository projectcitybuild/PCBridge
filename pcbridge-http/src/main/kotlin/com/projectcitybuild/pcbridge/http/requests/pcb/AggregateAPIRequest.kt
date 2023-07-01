package com.projectcitybuild.pcbridge.http.requests.pcb

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AggregateAPIRequest {

    @GET("v2/minecraft/{uuid}/aggregate")
    suspend fun get(
        @Path(value = "uuid") uuid: String,
        @Query("ip") ip: String,
    ): com.projectcitybuild.pcbridge.http.responses.ApiResponse<Aggregate>
}
