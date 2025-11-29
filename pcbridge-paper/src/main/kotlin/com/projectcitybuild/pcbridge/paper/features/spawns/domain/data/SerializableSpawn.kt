package com.projectcitybuild.pcbridge.paper.features.spawns.domain.data

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class SerializableSpawn(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {
    fun toLocation(world: World)
        = Location(world, x, y, z, yaw, pitch)

    companion object {
        fun fromLocation(location: Location)
            = SerializableSpawn(
                location.x,
                location.y,
                location.z,
                location.yaw,
                location.pitch,
            )
    }
}