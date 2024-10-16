package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class DonationPerk(
    @SerializedName("donation_perks_id")
    val id: Int = Math.random().toInt(),
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("expires_at")
    val expiresAt: Long =
        LocalDate.now()
            .plusMonths(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond(),
    @SerializedName("donation_tier")
    val donationTier: DonationTier,
)
