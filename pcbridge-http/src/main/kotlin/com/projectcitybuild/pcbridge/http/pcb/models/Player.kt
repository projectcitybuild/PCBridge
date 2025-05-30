package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Player(
    @SerializedName("player_minecraft_id")
    val id: Int,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("alias")
    val alias: String? = null,

    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("last_seen_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val lastSeenAt: LocalDateTime? = null,

    @SerializedName("last_synced_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val lastSyncedAt: LocalDateTime? = null,
)
