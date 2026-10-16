package com.projectcitybuild.pcbridge.platform.paper.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class SerializableLocationTest {
    @Test
    fun `from location should copy world and position data`() {
        val world = mock<World>()
        val worldId = UUID.fromString("51a62d2d-08e8-4bfa-938c-64ba71c4b81c")
        whenever(world.uid).thenReturn(worldId)
        whenever(world.name).thenReturn("world")
        val location = Location(world, 12.5, 64.0, -8.25, 90.0f, -15.0f)

        val result = SerializableLocation.fromLocation(location, world)

        assertEquals(
            SerializableLocation(
                worldId = "51a62d2d-08e8-4bfa-938c-64ba71c4b81c",
                worldName = "world",
                x = 12.5,
                y = 64.0,
                z = -8.25,
                yaw = 90.0f,
                pitch = -15.0f,
            ),
            result,
        )
    }

    @Test
    fun `serializable location should round trip through json`() {
        val location = SerializableLocation(
            worldId = "51a62d2d-08e8-4bfa-938c-64ba71c4b81c",
            worldName = "world",
            x = 12.5,
            y = 64.0,
            z = -8.25,
            yaw = 90.0f,
            pitch = -15.0f,
        )

        val encoded = Json.encodeToString(location)

        assertEquals(
            location,
            Json.decodeFromString<SerializableLocation>(encoded),
        )
    }
}
