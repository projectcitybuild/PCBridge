package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("id") val id: Int,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("aliases") val aliases: List<PlayerAlias>?,
)
