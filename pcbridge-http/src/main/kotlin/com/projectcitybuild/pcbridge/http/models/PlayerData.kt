package com.projectcitybuild.pcbridge.http.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    @SerializedName("account")
    val account: Account? = null,

    @SerializedName("player")
    val player: Player? = null,

    @SerializedName("ban")
    val playerBan: PlayerBan? = null,

    @SerializedName("ip_ban")
    val ipBan: IPBan? = null,

    @SerializedName("badges")
    val badges: List<Badge> = emptyList(),

    @SerializedName("donation_tiers")
    val donationPerks: List<DonationPerk> = emptyList(),
)
