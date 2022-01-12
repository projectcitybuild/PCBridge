package com.projectcitybuild.entities

import com.projectcitybuild.old_modules.storage.serializers.SerializableDate
import com.projectcitybuild.old_modules.storage.serializers.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class Warp(
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