package com.projectcitybuild.core.network.pcb.requests

import com.projectcitybuild.core.entities.models.*
import retrofit2.http.*

interface DonorApiInterface {

    @GET("minecraft/{uuid}/donation-tiers")
    suspend fun getDonationTier(
        @Path(value = "uuid") uuid: String
    ) : ApiResponse<DonationPerk>

    @GET("minecraft/{uuid}/boxes")
    suspend fun getAvailableBoxes(
        @Path(value = "uuid") uuid: String
    ) : ApiResponse<AvailableLootBoxes>

    @FormUrlEncoded
    @POST("minecraft/{uuid}}/boxes/redeem")
    suspend fun redeemAvailableBoxes() : ApiResponse<LootBoxRedememption>
}