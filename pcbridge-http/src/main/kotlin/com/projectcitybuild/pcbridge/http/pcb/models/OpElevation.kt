package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class OpElevation(
    @SerializedName("reason")
    val reason: String,

    @SerializedName("started_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val startedAt: LocalDateTime,

    @SerializedName("endedAt")
    @Serializable(with = LocalDateTimeSerializer::class)
    val endedAt: LocalDateTime,
)