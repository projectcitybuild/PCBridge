package com.projectcitybuild.entities.responses

import DonationPerk
import com.google.gson.annotations.SerializedName

data class Aggregate(
    @SerializedName("account") val account: Account? = null,
    @SerializedName("ban") val playerBan: PlayerBan? = null,
    @SerializedName("ip_ban") val ipBan: IPBan? = null,
    @SerializedName("badges") val badges: List<Badge> = emptyList(),
    @SerializedName("donation_tiers") val donationPerks: List<DonationPerk> = emptyList(),
)