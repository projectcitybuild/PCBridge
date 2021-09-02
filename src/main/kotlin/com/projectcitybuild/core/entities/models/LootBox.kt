package com.projectcitybuild.core.entities.models

import com.google.gson.annotations.SerializedName

data class LootBox(
        @SerializedName("minecraft_loot_box_id") val id: Int,
        @SerializedName("donation_tier_id") val donationTierId: Int,
        @SerializedName("loot_box_name") val name: String,
        @SerializedName("quantity") val quantity: Int,
        @SerializedName("is_active") val isActive: Int
)