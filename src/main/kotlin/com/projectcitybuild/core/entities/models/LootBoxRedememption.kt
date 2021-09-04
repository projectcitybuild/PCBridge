package com.projectcitybuild.core.entities.models

import com.google.gson.annotations.SerializedName

data class LootBoxRedememption(
        @SerializedName("redeemed_boxes") val redeemedBoxes: List<LootBox>?,
        @SerializedName("seconds_until_redeemable") val secondsUntilRedeemable: Int?
)