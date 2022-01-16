package com.projectcitybuild.modules.network.pcb.requests

import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.AvailableLootBoxes
import com.projectcitybuild.entities.responses.DonationPerk
import com.projectcitybuild.entities.responses.LootBoxRedememption
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DonorApiInterface {

    @GET("minecraft/{uuid}/donation-tiers")
    suspend fun getDonationTier(
        @Path(value = "uuid") uuid: String
    ) : ApiResponse<Array<DonationPerk>>

    @GET("minecraft/{uuid}/boxes")
    suspend fun getAvailableBoxes(
        @Path(value = "uuid") uuid: String
    ) : ApiResponse<AvailableLootBoxes>

    @POST("minecraft/{uuid}/boxes/redeem")
    suspend fun redeemAvailableBoxes(
        @Path(value = "uuid") uuid: String
    ) : ApiResponse<LootBoxRedememption>
}