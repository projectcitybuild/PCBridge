package com.projectcitybuild.entities

import com.projectcitybuild.entities.serializables.SerializableDate
import com.projectcitybuild.entities.serializables.SerializableUUID
import kotlinx.serialization.Serializable

@Deprecated("Use Warp")
@Serializable
data class LegacyWarp(
    val serverName: String,
    val worldName: String,
    val playerUUID: SerializableUUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val createdAt: SerializableDate,
)

