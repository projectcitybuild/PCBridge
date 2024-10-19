package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DonationTier(
    @SerializedName("donation_tier_id")
    val id: Int,

    @SerializedName("name")
    val name: String,
)
