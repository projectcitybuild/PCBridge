package com.projectcitybuild.entities.requests.pcb

import com.projectcitybuild.entities.responses.Aggregate
import com.projectcitybuild.entities.responses.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AggregateAPIRequest {

    @GET("v2/minecraft/{uuid}/aggregate")
    suspend fun get(
        @Path(value = "uuid") uuid: String,
    ): ApiResponse<Aggregate>
}
