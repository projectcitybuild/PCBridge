package com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.core.http.serialization.serializable.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class HttpOpElevation(
    @SerializedName("player_id")
    val playerId: Long,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("started_at")
    @Serializable(with = InstantSerializer::class)
    val startedAt: Instant,

    @SerializedName("ended_at")
    @Serializable(with = InstantSerializer::class)
    val endedAt: Instant,
)
