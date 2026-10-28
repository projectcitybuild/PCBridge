package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderLimits
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class WorldBorderGeometry(
    private val border: WorldBorder,
) {
    data class ChunkCoordinate(
        val x: Int,
        val z: Int,
    )

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
        if (!x.isFinite() || !z.isFinite()) return false

        val deltaX = x - border.centerX
        val deltaZ = z - border.centerZ

        return when (border) {
            is WorldBorder.Square, is WorldBorder.Rectangle -> {
                abs(deltaX) <= halfWidth && abs(deltaZ) <= halfDepth
            }
            is WorldBorder.Circle -> {
                val radius = border.radius
                hypot(deltaX, deltaZ) <= radius
            }
        }
    }

    fun sample(random: Random): Pair<Double, Double> = when (border) {
        is WorldBorder.Square, is WorldBorder.Rectangle -> {
            random.nextDouble(minX, maxX) to random.nextDouble(minZ, maxZ)
        }
        is WorldBorder.Circle -> {
            val radius = border.radius * sqrt(random.nextDouble())
            val angle = random.nextDouble(0.0, 2 * Math.PI)
            (border.centerX + radius * cos(angle)) to
                (border.centerZ + radius * sin(angle))
        }
    }

    fun clamp(
        x: Double,
        z: Double,
    ): Pair<Double, Double> {
        val safeX = x.takeIf(Double::isFinite) ?: border.centerX
        val safeZ = z.takeIf(Double::isFinite) ?: border.centerZ

        return when (border) {
            is WorldBorder.Square, is WorldBorder.Rectangle -> {
                clampCoordinate(safeX, minX, maxX, PLAYER_HALF_WIDTH) to
                    clampCoordinate(safeZ, minZ, maxZ, PLAYER_HALF_WIDTH)
            }
            is WorldBorder.Circle -> {
                val deltaX = safeX - border.centerX
                val deltaZ = safeZ - border.centerZ
                val radius = (border.radius - CIRCLE_PLAYER_MARGIN).coerceAtLeast(0.0)
                val distance = hypot(deltaX, deltaZ)
                if (distance <= radius || distance == 0.0) {
                    safeX to safeZ
                } else {
                    val scale = radius / distance
                    (border.centerX + deltaX * scale) to (border.centerZ + deltaZ * scale)
                }
            }
        }
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
        return hypot(deltaX, deltaZ) <= radius
    }

    fun chunkXRange(): IntRange = chunkCoordinateRange(minX)..chunkCoordinateRange(maxX)

    fun chunkCoordinates(): Sequence<ChunkCoordinate> = sequence {
        for (chunkX in chunkXRange()) {
            val chunkZRange = chunkZRange(chunkX) ?: continue
            for (chunkZ in chunkZRange) {
                if (chunkIntersects(chunkX, chunkZ)) yield(ChunkCoordinate(chunkX, chunkZ))
            }
        }
    }

    fun chunkZRange(chunkX: Int): IntRange? {
        if (border !is WorldBorder.Circle) {
            return chunkCoordinateRange(minZ)..chunkCoordinateRange(maxZ)
        }

        val minChunkX = chunkX * CHUNK_SIZE
        val nearestX = border.centerX.coerceIn(minChunkX, minChunkX + CHUNK_SIZE)
        val deltaX = nearestX - border.centerX
        val radius = border.radius
        if (abs(deltaX) > radius) return null

        val normalizedX = (deltaX / radius).coerceIn(-1.0, 1.0)
        val zOffset = radius * sqrt((1 - normalizedX * normalizedX).coerceAtLeast(0.0))
        return chunkCoordinateRange(border.centerZ - zOffset)..chunkCoordinateRange(border.centerZ + zOffset)
    }

    companion object {
        const val MAX_RADIUS = WorldBorderLimits.MAX_RADIUS
        const val MAX_DIMENSION = WorldBorderLimits.MAX_DIMENSION

        private const val CHUNK_SIZE = 16.0
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

            return if (safeMin > safeMax) {
                (min + max) / 2
            } else {
                value.coerceIn(safeMin, safeMax)
            }
        }
    }
}
