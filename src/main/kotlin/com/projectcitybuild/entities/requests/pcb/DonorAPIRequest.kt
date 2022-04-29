package com.projectcitybuild.entities.requests.pcb

import DonationPerk
import com.projectcitybuild.entities.responses.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DonorAPIRequest {

    @GET("v2/minecraft/{uuid}/donation-tiers")
    suspend fun getDonationTier(
        @Path(value = "uuid") uuid: String
    ): ApiResponse<Array<DonationPerk>>
}
