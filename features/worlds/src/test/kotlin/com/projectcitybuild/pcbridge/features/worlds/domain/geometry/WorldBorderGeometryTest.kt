package com.projectcitybuild.pcbridge.features.worlds.domain.geometry

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import org.bukkit.Location
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class WorldBorderGeometryTest {
    private val border =
        WorldBorder.Square(
            centerX = 10.0,
            centerZ = -5.0,
            radius = 20.0,
        )

    @Test
    fun `contains locations on square border edges and inside`() {
        assertTrue(border.geometry().contains(-10.0, -25.0))
        assertTrue(border.geometry().contains(30.0, 15.0))
        assertTrue(border.geometry().contains(10.0, -5.0))
    }

    @Test
    fun `rejects locations beyond any square border edge`() {
        assertFalse(border.geometry().contains(-10.01, -5.0))
        assertFalse(border.geometry().contains(10.0, 15.01))
    }

    @Test
    fun `circular border uses radial distance instead of square edges`() {
        val circle = WorldBorder.Circle(border.centerX, border.centerZ, border.radius)

        assertTrue(circle.geometry().contains(22.0, 4.0))
        assertFalse(circle.geometry().contains(25.0, 10.0))
    }

    @Test
    fun `rectangle uses full width and depth centered on the configured coordinates`() {
        val rectangle =
            WorldBorder.Rectangle(
                centerX = 0.0,
                centerZ = 0.0,
                width = 32.0,
                depth = 16.0,
            )

        assertTrue(rectangle.geometry().contains(16.0, 8.0))
        assertTrue(rectangle.geometry().contains(-16.0, -8.0))
        assertFalse(rectangle.geometry().contains(16.01, 0.0))
        assertFalse(rectangle.geometry().contains(0.0, 8.01))
    }

    @Test
    fun `clamps location coordinates while preserving other location values`() {
        val location = Location(mock(World::class.java), 40.0, 64.0, -30.0, 45.0f, 10.0f)

        val clamped = border.geometry().clamp(location)

        assertEquals(29.7, clamped.x)
        assertEquals(64.0, clamped.y)
        assertEquals(-24.7, clamped.z)
        assertEquals(45.0f, clamped.yaw)
        assertEquals(10.0f, clamped.pitch)
    }

    @Test
    fun `clamps a rectangular border location to each dimension`() {
        val rectangle =
            WorldBorder.Rectangle(
                centerX = 0.0,
                centerZ = 0.0,
                width = 32.0,
                depth = 16.0,
            )
        val location = Location(mock(World::class.java), 30.0, 64.0, 20.0)

        val clamped = rectangle.geometry().clamp(location)

        assertEquals(15.7, clamped.x)
        assertEquals(7.7, clamped.z)
    }

    @Test
    fun `clamps a circular border location radially`() {
        val circle =
            WorldBorder.Circle(
                0.0,
                0.0,
                radius = 10.0,
            )
        val location = Location(mock(World::class.java), 20.0, 64.0, 20.0)

        val clamped = circle.geometry().clamp(location)

        assertEquals(10.0 - 0.3 * kotlin.math.sqrt(2.0), kotlin.math.hypot(clamped.x, clamped.z), 1e-9)
    }

    @Test
    fun `circular fill bounds skip chunk columns outside the circle`() {
        val circle =
            WorldBorder.Circle(
                0.0,
                0.0,
                radius = 10.0,
            )

        assertTrue(circle.geometry().chunkIntersects(0, 0))
        assertFalse(circle.geometry().chunkIntersects(1, 1))
        assertTrue(circle.geometry().chunkZRange(0) != null)
        assertNull(circle.geometry().chunkZRange(1))
    }

    @Test
    fun `rectangular fill bounds use full width and depth`() {
        val rectangle =
            WorldBorder.Rectangle(
                0.0,
                0.0,
                width = 32.0,
                depth = 16.0,
            )

        assertTrue(rectangle.geometry().chunkIntersects(1, 0))
        assertEquals(-1..0, rectangle.geometry().chunkZRange(0))
    }

    @Test
    fun `validates border dimensions and supported world range`() {
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(0.0, 0.0, radius = 0.0)
                .geometry()
                .validate()
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(Double.NaN, 0.0, radius = 1.0)
                .geometry()
                .validate()
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(
                0.0,
                0.0,
                radius = WorldBorderGeometry.MAX_RADIUS + 1,
            ).geometry().validate()
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(
                WorldBorderGeometry.MAX_RADIUS,
                0.0,
                radius = 1.0,
            ).geometry().validate()
        }
    }

    private fun WorldBorder.geometry() = WorldBorderGeometry(this)
}
