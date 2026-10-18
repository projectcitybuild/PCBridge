package com.projectcitybuild.pcbridge.platform.paper.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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

    @Test
    fun `to location resolves world by uuid and preserves coordinates`() {
        val server = mock<Server>()
        val world = mock<World>()
        val worldId = UUID.fromString("51a62d2d-08e8-4bfa-938c-64ba71c4b81c")
        whenever(server.getWorld(worldId)).thenReturn(world)
        val serializableLocation = SerializableLocation(
            worldId = worldId.toString(),
            worldName = "renamed-world",
            x = 12.5,
            y = 64.25,
            z = -8.75,
            yaw = 90.0f,
            pitch = -15.0f,
        )

        val result = serializableLocation.toLocation(server)

        assertEquals(Location(world, 12.5, 64.25, -8.75, 90.0f, -15.0f), result)
    }

    @Test
    fun `to location returns null for malformed or unavailable world uuid`() {
        val server = mock<Server>()
        val malformed = SerializableLocation("not-a-uuid", "world", 0.0, 0.0, 0.0, 0.0f, 0.0f)
        val unavailable = SerializableLocation(
            UUID.randomUUID().toString(),
            "world",
            0.0,
            0.0,
            0.0,
            0.0f,
            0.0f,
        )

        assertNull(malformed.toLocation(server))
        assertNull(unavailable.toLocation(server))
        verify(server, never()).getWorld("world")
    }
}
