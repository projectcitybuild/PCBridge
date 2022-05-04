package com.projectcitybuild.entities

import org.bukkit.Location

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
        fun fromLocation(serverName: String, location: Location): SerializableLocation {
            return SerializableLocation(
                serverName = serverName,
                worldName = location.world.name,
                x = location.x,
                y = location.y,
                z = location.z,
                pitch = location.pitch,
                yaw = location.yaw
            )
        }
    }
}
