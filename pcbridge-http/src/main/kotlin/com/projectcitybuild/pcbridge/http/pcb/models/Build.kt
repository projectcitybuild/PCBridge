package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Build(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("lore")
    val lore: String?,

    @SerializedName("votes")
    val votes: Int,

    @SerializedName("world")
    val world: String,

    @SerializedName("x")
    val x: Double,

    @SerializedName("y")
    val y: Double,

    @SerializedName("z")
    val z: Double,

    @SerializedName("pitch")
    val pitch: Float,

    @SerializedName("yaw")
    val yaw: Float,

    @SerializedName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @SerializedName("updated_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,

    @SerializedName("player")
    val player: Player?,
)
