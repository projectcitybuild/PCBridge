package com.projectcitybuild.pcbridge.paper.core.support.spigot

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SerializableLocation(
    val worldId: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    companion object {
        fun fromLocation(location: Location, world: World) = SerializableLocation(
            worldId = world.uid.toString(),
            worldName = world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            yaw = location.yaw,
            pitch = location.pitch,
        )
    }
}