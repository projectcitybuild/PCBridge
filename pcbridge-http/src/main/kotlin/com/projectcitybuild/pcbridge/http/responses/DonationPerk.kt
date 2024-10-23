package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class DonationPerk(
    @SerializedName("donation_perks_id")
    val id: Int,

    @SerializedName("is_active")
    val isActive: Boolean,

    @SerializedName("expires_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime,

    @SerializedName("donation_tier")
    val donationTier: DonationTier,
)
