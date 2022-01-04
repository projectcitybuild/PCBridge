package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class DonationTier(
        @SerializedName("donation_tier_id") val id: Int,
        @SerializedName("name") val name: String
)