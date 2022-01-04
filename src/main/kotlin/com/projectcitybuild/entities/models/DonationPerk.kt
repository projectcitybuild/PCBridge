package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class DonationPerk(
        @SerializedName("donation_perks_id") val id: Int,
        @SerializedName("is_active") val isActive: Boolean,
        @SerializedName("expires_at") val expiresAt: Long,
        @SerializedName("donation_tier") val donationTier: DonationTier
)