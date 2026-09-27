package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Warp(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

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
)
