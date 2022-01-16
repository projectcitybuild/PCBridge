package com.projectcitybuild.entities.serializables

import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class SerializableLocation(
    val serverName: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
) {
    companion object {
        fun fromLocation(location: Location, serverName: String): SerializableLocation {
            return SerializableLocation(
                serverName,
                location.world.name,
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw,
            )
        }
    }
}
