package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.InstantSerializer
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
