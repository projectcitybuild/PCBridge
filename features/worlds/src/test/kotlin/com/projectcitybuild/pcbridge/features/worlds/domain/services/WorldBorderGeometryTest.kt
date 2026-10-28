package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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
    fun `samples square rectangle and circle positions inside exact geometry`() {
        val borders = listOf(
            WorldBorder.Square(-20.0, 12.0, radius = 8.0),
            WorldBorder.Rectangle(-20.0, 12.0, width = 12.0, depth = 4.0),
            WorldBorder.Circle(-20.0, 12.0, radius = 8.0),
        )

        borders.forEach { sampledBorder ->
            val random = Random(42)
            repeat(500) {
                val (x, z) = sampledBorder.geometry.sample(random)
                assertTrue(sampledBorder.geometry.contains(x, z))
            }
        }
    }

    @Test
    fun `circle sampling uses square root radius for uniform area`() {
        val circle = WorldBorder.Circle(-20.0, 12.0, radius = 8.0)
        val random = Random(42)
        val expectedRandom = Random(42)
        val radius = circle.radius * sqrt(expectedRandom.nextDouble())
        val angle = expectedRandom.nextDouble(0.0, 2 * Math.PI)

        val (x, z) = circle.geometry.sample(random)

        assertEquals(circle.centerX + radius * cos(angle), x)
        assertEquals(circle.centerZ + radius * sin(angle), z)
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
    fun `clamps coordinates to keep a player inside the square border`() {
        val (x, z) = border.geometry().clamp(40.0, -30.0)

        assertEquals(29.7, x)
        assertEquals(-24.7, z)
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
        val (x, z) = rectangle.geometry().clamp(30.0, 20.0)

        assertEquals(15.7, x)
        assertEquals(7.7, z)
    }

    @Test
    fun `clamps a circular border location radially`() {
        val circle =
            WorldBorder.Circle(
                0.0,
                0.0,
                radius = 10.0,
            )
        val (x, z) = circle.geometry().clamp(20.0, 20.0)

        assertEquals(10.0 - 0.3 * sqrt(2.0), hypot(x, z), 1e-9)
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
    }

    @Test
    fun `chunk coordinate iteration emits only intersecting chunks without duplicates`() {
        val geometry = WorldBorder.Circle(0.0, 0.0, 20.0).geometry
        val chunks = geometry.chunkCoordinates().toList()

        assertEquals(chunks.size, chunks.distinct().size)
        assertTrue(chunks.all { geometry.chunkIntersects(it.x, it.z) })
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
        assertThrows<IllegalArgumentException> { WorldBorder.Square(0.0, 0.0, radius = 0.0) }
        assertThrows<IllegalArgumentException> { WorldBorder.Square(0.0, 0.0, radius = -1.0) }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(Double.NaN, 0.0, radius = 1.0)
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Circle(Double.POSITIVE_INFINITY, 0.0, radius = 1.0)
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Rectangle(0.0, 0.0, width = Double.POSITIVE_INFINITY, depth = 1.0)
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Rectangle(0.0, 0.0, width = 1.0, depth = -1.0)
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(
                0.0,
                0.0,
                radius = WorldBorderGeometry.MAX_RADIUS + 1,
            )
        }
        assertThrows<IllegalArgumentException> {
            WorldBorder.Square(
                WorldBorderGeometry.MAX_RADIUS,
                0.0,
                radius = 1.0,
            )
        }
    }

    @Test
    fun `rejects dimensions that collapse at the configured floating point center`() {
        assertThrows<IllegalArgumentException> {
            WorldBorder.Rectangle(10_000_000.0, 0.0, width = 1e-10, depth = 1.0)
        }
    }

    @Test
    fun `circle containment remains accurate when squared distances underflow`() {
        val circle = WorldBorder.Circle(0.0, 0.0, radius = 1e-200)

        assertTrue(circle.geometry.contains(1e-200, 0.0))
        assertFalse(circle.geometry.contains(2e-200, 0.0))
    }

    @Test
    fun `tiny circle at chunk boundaries includes every intersecting chunk`() {
        val geometry = WorldBorder.Circle(0.0, 0.0, radius = 1e-200).geometry

        assertEquals(
            setOf(
                WorldBorderGeometry.ChunkCoordinate(-1, -1),
                WorldBorderGeometry.ChunkCoordinate(-1, 0),
                WorldBorderGeometry.ChunkCoordinate(0, -1),
                WorldBorderGeometry.ChunkCoordinate(0, 0),
            ),
            geometry.chunkCoordinates().toSet(),
        )
    }

    @Test
    fun `world limit edges map to the expected chunk range`() {
        val border = WorldBorder.Square(0.0, 0.0, WorldBorderGeometry.MAX_RADIUS)

        assertEquals(
            -1_874_999..1_874_999,
            border.geometry.chunkXRange(),
        )
        assertTrue(border.geometry.contains(-WorldBorderGeometry.MAX_RADIUS, 0.0))
        assertTrue(border.geometry.contains(WorldBorderGeometry.MAX_RADIUS, 0.0))
    }

    @Test
    fun `circle and rectangle boundaries are exact at negative coordinates`() {
        val circle = WorldBorder.Circle(-16.0, -16.0, radius = 10.0)
        val rectangle = WorldBorder.Rectangle(-16.0, -16.0, width = 32.0, depth = 16.0)

        assertTrue(circle.geometry.contains(-6.0, -16.0))
        assertFalse(circle.geometry.contains(-5.9999, -16.0))
        assertTrue(rectangle.geometry.contains(0.0, -24.0))
        assertFalse(rectangle.geometry.contains(0.0001, -24.0))
    }

    @Test
    fun `clamping non-finite coordinates returns finite values`() {
        val (x, z) = border.geometry().clamp(Double.NaN, Double.POSITIVE_INFINITY)

        assertTrue(x.isFinite())
        assertTrue(z.isFinite())
    }

    @Test
    fun `reuses cached geometry and handles a zero-distance circle clamp`() {
        val circle = WorldBorder.Circle(0.0, 0.0, radius = 10.0)
        val (x, z) = circle.geometry.clamp(0.0, 0.0)

        assertSame(circle.geometry, circle.geometry)
        assertEquals(0.0, x)
        assertEquals(0.0, z)
    }

    private fun WorldBorder.geometry() = WorldBorderGeometry(this)
}
