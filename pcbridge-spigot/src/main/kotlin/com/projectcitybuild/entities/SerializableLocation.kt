package com.projectcitybuild.entities

import org.bukkit.Location

data class SerializableLocation(
    val worldName: String = "world_name",
    val x: Double = 1.0,
    val y: Double = 2.0,
    val z: Double = 3.0,
    val pitch: Float = 4f,
    val yaw: Float = 5f,
) {
    companion object {
        fun fromLocation(location: Location): SerializableLocation {
            return SerializableLocation(
                worldName = location.world!!.name,
                x = location.x,
                y = location.y,
                z = location.z,
                pitch = location.pitch,
                yaw = location.yaw
            )
        }
    }
}
