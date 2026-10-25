package com.projectcitybuild.pcbridge.features.worlds.domain.data

import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class SerializableWorldBorderTest {
    private val border =
        SerializableWorldBorder(
            centerX = 10.0,
            centerZ = -5.0,
            shape = WorldBorderShape.SQUARE,
            radius = 20.0,
        )

    @Test
    fun `contains locations on square border edges and inside`() {
        assertTrue(border.contains(-10.0, -25.0))
        assertTrue(border.contains(30.0, 15.0))
        assertTrue(border.contains(10.0, -5.0))
    }

    @Test
    fun `rejects locations beyond any square border edge`() {
        assertFalse(border.contains(-10.01, -5.0))
        assertFalse(border.contains(10.0, 15.01))
    }

    @Test
    fun `circular border uses radial distance instead of square edges`() {
        val circle = border.copy(shape = WorldBorderShape.CIRCLE)

        assertTrue(circle.contains(22.0, 4.0))
        assertFalse(circle.contains(25.0, 10.0))
    }

    @Test
    fun `rectangle uses full width and depth centered on the configured coordinates`() {
        val rectangle =
            SerializableWorldBorder(
                centerX = 0.0,
                centerZ = 0.0,
                shape = WorldBorderShape.RECTANGLE,
                width = 32.0,
                depth = 16.0,
            )

        assertTrue(rectangle.contains(16.0, 8.0))
        assertTrue(rectangle.contains(-16.0, -8.0))
        assertFalse(rectangle.contains(16.01, 0.0))
        assertFalse(rectangle.contains(0.0, 8.01))
    }

    @Test
    fun `clamps location coordinates while preserving other location values`() {
        val location = Location(mock(World::class.java), 40.0, 64.0, -30.0, 45.0f, 10.0f)

        val clamped = border.clamp(location)

        assertEquals(30.0, clamped.x)
        assertEquals(64.0, clamped.y)
        assertEquals(-25.0, clamped.z)
        assertEquals(45.0f, clamped.yaw)
        assertEquals(10.0f, clamped.pitch)
    }

    @Test
    fun `clamps a rectangular border location to each dimension`() {
        val rectangle =
            SerializableWorldBorder(
                centerX = 0.0,
                centerZ = 0.0,
                shape = WorldBorderShape.RECTANGLE,
                width = 32.0,
                depth = 16.0,
            )
        val location = Location(mock(World::class.java), 30.0, 64.0, 20.0)

        val clamped = rectangle.clamp(location)

        assertEquals(16.0, clamped.x)
        assertEquals(8.0, clamped.z)
    }

    @Test
    fun `clamps a circular border location radially`() {
        val circle =
            SerializableWorldBorder(
                0.0,
                0.0,
                shape = WorldBorderShape.CIRCLE,
                radius = 10.0,
            )
        val location = Location(mock(World::class.java), 20.0, 64.0, 20.0)

        val clamped = circle.clamp(location)

        assertEquals(10.0, kotlin.math.hypot(clamped.x, clamped.z), 1e-9)
    }

    @Test
    fun `circular fill bounds skip chunk columns outside the circle`() {
        val circle =
            SerializableWorldBorder(
                0.0,
                0.0,
                shape = WorldBorderShape.CIRCLE,
                radius = 10.0,
            )

        assertTrue(circle.chunkIntersects(0, 0))
        assertFalse(circle.chunkIntersects(1, 1))
        assertTrue(circle.chunkZRange(0) != null)
        assertNull(circle.chunkZRange(1))
    }

    @Test
    fun `rectangular fill bounds use full width and depth`() {
        val rectangle =
            SerializableWorldBorder(
                0.0,
                0.0,
                shape = WorldBorderShape.RECTANGLE,
                width = 32.0,
                depth = 16.0,
            )

        assertTrue(rectangle.chunkIntersects(1, 0))
        assertEquals(-1..0, rectangle.chunkZRange(0))
    }

    @Test
    fun `validates required shape and shape dimensions`() {
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(0.0, 0.0, shape = null, radius = 1.0).validate()
        }
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(0.0, 0.0, shape = WorldBorderShape.SQUARE, radius = 0.0).validate()
        }
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(Double.NaN, 0.0, shape = WorldBorderShape.SQUARE, radius = 1.0).validate()
        }
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(
                0.0,
                0.0,
                shape = WorldBorderShape.SQUARE,
                radius = SerializableWorldBorder.MAX_RADIUS + 1,
            ).validate()
        }
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(
                SerializableWorldBorder.MAX_RADIUS,
                0.0,
                shape = WorldBorderShape.SQUARE,
                radius = 1.0,
            ).validate()
        }
        assertThrows<IllegalArgumentException> {
            SerializableWorldBorder(0.0, 0.0, shape = WorldBorderShape.RECTANGLE, width = 16.0).validate()
        }
    }
}
