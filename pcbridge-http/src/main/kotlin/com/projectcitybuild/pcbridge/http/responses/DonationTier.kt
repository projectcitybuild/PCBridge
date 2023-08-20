package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName

data class DonationTier(
    @SerializedName("donation_tier_id") val id: Int = Math.random().toInt(),
    @SerializedName("name") val name: String = "name"
)
