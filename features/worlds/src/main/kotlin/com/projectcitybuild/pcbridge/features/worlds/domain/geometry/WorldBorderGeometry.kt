package com.projectcitybuild.pcbridge.features.worlds.domain.geometry

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import org.bukkit.Location
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

class WorldBorderGeometry(
    private val border: WorldBorder,
) {
    private val halfWidth: Double
        get() = when (border) {
            is WorldBorder.Square -> border.radius
            is WorldBorder.Circle -> border.radius
            is WorldBorder.Rectangle -> border.width / 2
        }

    private val halfDepth: Double
        get() = when (border) {
            is WorldBorder.Square -> border.radius
            is WorldBorder.Circle -> border.radius
            is WorldBorder.Rectangle -> border.depth / 2
        }

    val minX: Double
        get() = border.centerX - halfWidth

    val maxX: Double
        get() = border.centerX + halfWidth

    val minZ: Double
        get() = border.centerZ - halfDepth

    val maxZ: Double
        get() = border.centerZ + halfDepth

    val nativeBorderSize: Double
        get() = max(halfWidth, halfDepth) * 2

    fun contains(
        x: Double,
        z: Double,
    ): Boolean {
        val deltaX = x - border.centerX
        val deltaZ = z - border.centerZ

        return when (border) {
            is WorldBorder.Square, is WorldBorder.Rectangle -> {
                abs(deltaX) <= halfWidth && abs(deltaZ) <= halfDepth
            }
            is WorldBorder.Circle -> {
                val radius = border.radius
                deltaX * deltaX + deltaZ * deltaZ <= radius * radius
            }
        }
    }

    fun clamp(location: Location): Location {
        val clamped = location.clone()
        when (border) {
            is WorldBorder.Square, is WorldBorder.Rectangle -> {
                clamped.x = clampCoordinate(clamped.x, minX, maxX, PLAYER_HALF_WIDTH)
                clamped.z = clampCoordinate(clamped.z, minZ, maxZ, PLAYER_HALF_WIDTH)
            }
            is WorldBorder.Circle -> {
                val deltaX = clamped.x - border.centerX
                val deltaZ = clamped.z - border.centerZ
                val radius = border.radius
                val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
                val safeRadius = (radius - CIRCLE_PLAYER_MARGIN).coerceAtLeast(0.0)
                if (distance > safeRadius) {
                    val scale = safeRadius / distance
                    clamped.x = border.centerX + deltaX * scale
                    clamped.z = border.centerZ + deltaZ * scale
                }
            }
        }
        return clamped
    }

    fun chunkIntersects(
        chunkX: Int,
        chunkZ: Int,
    ): Boolean {
        if (border !is WorldBorder.Circle) return true

        val minX = chunkX * CHUNK_SIZE
        val minZ = chunkZ * CHUNK_SIZE
        val nearestX = border.centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val nearestZ = border.centerZ.coerceIn(minZ, minZ + CHUNK_SIZE)
        val deltaX = nearestX - border.centerX
        val deltaZ = nearestZ - border.centerZ
        val radius = border.radius
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius
    }

    fun chunkZRange(chunkX: Int): IntRange? {
        if (border !is WorldBorder.Circle) {
            return chunkCoordinateRange(minZ)..chunkCoordinateRange(maxZ)
        }

        val radius = border.radius
        val minX = chunkX * CHUNK_SIZE
        val nearestX = border.centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val deltaX = nearestX - border.centerX
        val remainingRadiusSquared = radius * radius - deltaX * deltaX
        if (remainingRadiusSquared < 0) return null

        val zOffset = sqrt(remainingRadiusSquared)
        return chunkCoordinateRange(border.centerZ - zOffset)..chunkCoordinateRange(border.centerZ + zOffset)
    }

    fun validate(): WorldBorder {
        require(border.centerX.isFinite() && border.centerZ.isFinite()) { "Border center must be finite" }

        when (border) {
            is WorldBorder.Square -> {
                val radius = border.radius
                require(radius.isFinite() && radius > 0 && radius <= MAX_RADIUS) {
                    "Border radius is out of range"
                }
            }
            is WorldBorder.Circle -> {
                val radius = border.radius
                require(radius.isFinite() && radius > 0 && radius <= MAX_RADIUS) {
                    "Border radius is out of range"
                }
            }
            is WorldBorder.Rectangle -> {
                val width = border.width
                val depth = border.depth
                require(width.isFinite() && width > 0 && width <= MAX_DIMENSION) {
                    "Rectangle width is out of range"
                }
                require(depth.isFinite() && depth > 0 && depth <= MAX_DIMENSION) {
                    "Rectangle depth is out of range"
                }
            }
        }

        require(
            minX >= -MAX_RADIUS &&
                maxX <= MAX_RADIUS &&
                minZ >= -MAX_RADIUS &&
                maxZ <= MAX_RADIUS,
        ) { "Border edges are outside the supported world range" }
        return border
    }

    companion object {
        const val MAX_RADIUS = 29_999_984.0
        const val MAX_DIMENSION = MAX_RADIUS * 2

        private const val CHUNK_SIZE = 16.0

        // Keep the player's 0.6-block-wide hitbox inside the border edge.
        private const val PLAYER_HALF_WIDTH = 0.3

        private val CIRCLE_PLAYER_MARGIN = PLAYER_HALF_WIDTH * sqrt(2.0)

        private fun chunkCoordinateRange(coordinate: Double) = floor(coordinate / CHUNK_SIZE).toInt()

        private fun clampCoordinate(
            value: Double,
            min: Double,
            max: Double,
            margin: Double,
        ): Double {
            val safeMin = min + margin
            val safeMax = max - margin
            return if (safeMin > safeMax) (min + max) / 2 else value.coerceIn(safeMin, safeMax)
        }
    }
}
