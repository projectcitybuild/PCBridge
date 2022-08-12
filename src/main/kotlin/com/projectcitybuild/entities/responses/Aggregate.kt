package com.projectcitybuild.entities.responses

import DonationPerk
import com.google.gson.annotations.SerializedName

data class Aggregate(
    @SerializedName("account") val account: Account?,
    @SerializedName("ban") val ban: GameBan?,
    @SerializedName("badges") val badges: List<Badge>,
    @SerializedName("donation_tiers") val donationPerks: List<DonationPerk>,
)
