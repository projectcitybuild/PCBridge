package com.projectcitybuild.modules.network.pcb.requests

import com.projectcitybuild.entities.responses.*
import retrofit2.http.*

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