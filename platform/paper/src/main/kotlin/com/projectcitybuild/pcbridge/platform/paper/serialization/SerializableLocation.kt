package com.projectcitybuild.pcbridge.platform.paper.serialization

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import java.util.UUID

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
        fun fromLocation(
            location: Location,
            world: World,
        ) = SerializableLocation(
            worldId = world.uid.toString(),
            worldName = world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            yaw = location.yaw,
            pitch = location.pitch,
        )
    }

    fun toLocation(server: Server): Location? {
        val worldId = try {
            UUID.fromString(worldId)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val world = server.getWorld(worldId) ?: return null
        return Location(world, x, y, z, yaw, pitch)
    }
}
