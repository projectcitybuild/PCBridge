package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName

data class PlayerWarning(
    @SerializedName("id")
    val id: Int,

    @SerializedName("warned_player_id")
    val warnedPlayerId: String,

    @SerializedName("warner_player_id")
    val warnerPlayerId: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("additional_info")
    val additionalInfo: String?,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("is_acknowledged")
    val isAcknowledged: Boolean,

    @SerializedName("created_at")
    val createdAt: Long,

    @SerializedName("updated_at")
    val updatedAt: Long,

    @SerializedName("acknowledged_at")
    val acknowledgedAt: Long?,
)
