package com.projectcitybuild.core.entities.models

import com.google.gson.annotations.SerializedName

data class AvailableLootBoxes(
        @SerializedName("redeemable_boxes") val redeemableBoxes: List<LootBox>?,
        @SerializedName("seconds_until_redeemable") val secondsUntilRedeemable: Int?
)