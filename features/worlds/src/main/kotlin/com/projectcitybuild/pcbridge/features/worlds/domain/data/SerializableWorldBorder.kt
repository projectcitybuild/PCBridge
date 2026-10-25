package com.projectcitybuild.pcbridge.features.worlds.domain.data

import org.bukkit.Location
import kotlin.math.abs
import kotlin.math.sqrt

data class SerializableWorldBorder(
    val centerX: Double,
    val centerZ: Double,
    val radius: Double,
    val shape: WorldBorderShape? = null,
) {
    val effectiveShape: WorldBorderShape
        get() = shape ?: WorldBorderShape.SQUARE

    fun contains(
        x: Double,
        z: Double,
    ): Boolean {
        val deltaX = x - centerX
        val deltaZ = z - centerZ
        return when (effectiveShape) {
            WorldBorderShape.SQUARE -> abs(deltaX) <= radius && abs(deltaZ) <= radius
            WorldBorderShape.CIRCLE -> deltaX * deltaX + deltaZ * deltaZ <= radius * radius
        }
    }

    fun clamp(location: Location): Location {
        val clamped = location.clone()
        when (effectiveShape) {
            WorldBorderShape.SQUARE -> {
                clamped.x = clamped.x.coerceIn(centerX - radius, centerX + radius)
                clamped.z = clamped.z.coerceIn(centerZ - radius, centerZ + radius)
            }
            WorldBorderShape.CIRCLE -> {
                val deltaX = clamped.x - centerX
                val deltaZ = clamped.z - centerZ
                val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
                if (distance > radius) {
                    val scale = radius / distance
                    clamped.x = centerX + deltaX * scale
                    clamped.z = centerZ + deltaZ * scale
                }
            }
        }
        return clamped
    }

    fun chunkIntersects(
        chunkX: Int,
        chunkZ: Int,
    ): Boolean {
        if (effectiveShape == WorldBorderShape.SQUARE) return true

        val minX = chunkX * CHUNK_SIZE
        val minZ = chunkZ * CHUNK_SIZE
        val nearestX = centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val nearestZ = centerZ.coerceIn(minZ, minZ + CHUNK_SIZE)
        val deltaX = nearestX - centerX
        val deltaZ = nearestZ - centerZ
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius
    }

    fun chunkZRange(chunkX: Int): IntRange? {
        if (effectiveShape == WorldBorderShape.SQUARE) {
            return chunkCoordinateRange(centerZ - radius)..chunkCoordinateRange(centerZ + radius)
        }

        val minX = chunkX * CHUNK_SIZE
        val nearestX = centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val deltaX = nearestX - centerX
        val remainingRadiusSquared = radius * radius - deltaX * deltaX
        if (remainingRadiusSquared < 0) return null

        val zOffset = sqrt(remainingRadiusSquared)
        return chunkCoordinateRange(centerZ - zOffset)..chunkCoordinateRange(centerZ + zOffset)
    }

    fun validate(): SerializableWorldBorder {
        require(centerX.isFinite() && centerZ.isFinite()) { "Border center must be finite" }
        require(radius.isFinite() && radius > 0 && radius <= MAX_RADIUS) { "Border radius is out of range" }
        require(
            centerX - radius >= -MAX_RADIUS &&
                centerX + radius <= MAX_RADIUS &&
                centerZ - radius >= -MAX_RADIUS &&
                centerZ + radius <= MAX_RADIUS,
        ) { "Border edges are outside the supported world range" }
        return this
    }

    companion object {
        const val MAX_RADIUS = 29_999_984.0
        private const val CHUNK_SIZE = 16.0

        private fun chunkCoordinateRange(coordinate: Double) = kotlin.math.floor(coordinate / CHUNK_SIZE).toInt()
    }
}

enum class WorldBorderShape {
    SQUARE,
    CIRCLE,
}
