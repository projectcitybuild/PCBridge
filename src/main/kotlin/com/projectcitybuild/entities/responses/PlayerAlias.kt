package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class PlayerAlias(
    @SerializedName("id") val id: Int,
    @SerializedName("alias") val alias: String,
    @SerializedName("registered_at") val registeredAt: Long,
)
