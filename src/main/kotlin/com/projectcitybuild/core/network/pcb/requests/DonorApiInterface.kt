package com.projectcitybuild.core.network.pcb.requests

import com.projectcitybuild.core.entities.models.*
import retrofit2.Call
import retrofit2.http.*

interface DonorApiInterface {

    @GET("minecraft/{uuid}/donation-tiers")
    fun getDonationTier(
        @Path(value = "uuid") uuid: String
    ) : Call<ApiResponse<DonationPerk>>

    @GET("minecraft/{uuid}/boxes")
    fun getAvailableBoxes(
        @Path(value = "uuid") uuid: String
    ) : Call<ApiResponse<AvailableLootBoxes>>

    @FormUrlEncoded
    @POST("minecraft/{uuid}}/boxes/redeem")
    fun redeemAvailableBoxes() : Call<ApiResponse<LootBoxRedememption>>

}