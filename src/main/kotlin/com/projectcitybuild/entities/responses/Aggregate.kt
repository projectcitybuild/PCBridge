package com.projectcitybuild.entities.responses

import DonationPerk
import com.google.gson.annotations.SerializedName

data class Aggregate(
    @SerializedName("account") val account: Account?,
    @SerializedName("ban") val playerBan: PlayerBan?,
    @SerializedName("ip_ban") val ipBan: IPBan?,
    @SerializedName("badges") val badges: List<Badge>,
    @SerializedName("donation_tiers") val donationPerks: List<DonationPerk>,
) {
    companion object {
        val stub: Aggregate
            get() = Aggregate(
                account = null,
                playerBan = null,
                ipBan = null,
                badges = emptyList(),
                donationPerks = emptyList(),
            )
    }
}
