package com.projectcitybuild.pcbridge.features.worlds.domain.data

import org.bukkit.Location
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

data class SerializableWorldBorder(
    val centerX: Double,
    val centerZ: Double,
    val shape: WorldBorderShape?,
    val radius: Double? = null,
    val width: Double? = null,
    val depth: Double? = null,
) {
    val effectiveShape: WorldBorderShape
        get() = requireNotNull(shape) { "Border shape must be specified." }

    val halfWidth: Double
        get() =
            when (effectiveShape) {
                WorldBorderShape.SQUARE, WorldBorderShape.CIRCLE -> requireNotNull(radius) { "Border radius must be specified." }
                WorldBorderShape.RECTANGLE -> requireNotNull(width) { "Rectangle width must be specified." } / 2
            }

    val halfDepth: Double
        get() =
            when (effectiveShape) {
                WorldBorderShape.SQUARE, WorldBorderShape.CIRCLE -> requireNotNull(radius) { "Border radius must be specified." }
                WorldBorderShape.RECTANGLE -> requireNotNull(depth) { "Rectangle depth must be specified." } / 2
            }

    val minX: Double
        get() = centerX - halfWidth

    val maxX: Double
        get() = centerX + halfWidth

    val minZ: Double
        get() = centerZ - halfDepth

    val maxZ: Double
        get() = centerZ + halfDepth

    val nativeBorderSize: Double
        get() = max(halfWidth, halfDepth) * 2

    fun contains(
        x: Double,
        z: Double,
    ): Boolean {
        val deltaX = x - centerX
        val deltaZ = z - centerZ
        return when (effectiveShape) {
            WorldBorderShape.SQUARE, WorldBorderShape.RECTANGLE -> abs(deltaX) <= halfWidth && abs(deltaZ) <= halfDepth
            WorldBorderShape.CIRCLE -> {
                val borderRadius = requireNotNull(radius) { "Border radius must be specified." }
                deltaX * deltaX + deltaZ * deltaZ <= borderRadius * borderRadius
            }
        }
    }

    fun clamp(location: Location): Location {
        val clamped = location.clone()
        when (effectiveShape) {
            WorldBorderShape.SQUARE, WorldBorderShape.RECTANGLE -> {
                clamped.x = clamped.x.coerceIn(minX, maxX)
                clamped.z = clamped.z.coerceIn(minZ, maxZ)
            }
            WorldBorderShape.CIRCLE -> {
                val deltaX = clamped.x - centerX
                val deltaZ = clamped.z - centerZ
                val borderRadius = requireNotNull(radius) { "Border radius must be specified." }
                val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
                if (distance > borderRadius) {
                    val scale = borderRadius / distance
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
        if (effectiveShape != WorldBorderShape.CIRCLE) return true

        val minX = chunkX * CHUNK_SIZE
        val minZ = chunkZ * CHUNK_SIZE
        val nearestX = centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val nearestZ = centerZ.coerceIn(minZ, minZ + CHUNK_SIZE)
        val deltaX = nearestX - centerX
        val deltaZ = nearestZ - centerZ
        val borderRadius = requireNotNull(radius) { "Border radius must be specified." }
        return deltaX * deltaX + deltaZ * deltaZ <= borderRadius * borderRadius
    }

    fun chunkZRange(chunkX: Int): IntRange? {
        if (effectiveShape != WorldBorderShape.CIRCLE) {
            return chunkCoordinateRange(minZ)..chunkCoordinateRange(maxZ)
        }

        val borderRadius = requireNotNull(radius) { "Border radius must be specified." }
        val minX = chunkX * CHUNK_SIZE
        val nearestX = centerX.coerceIn(minX, minX + CHUNK_SIZE)
        val deltaX = nearestX - centerX
        val remainingRadiusSquared = borderRadius * borderRadius - deltaX * deltaX
        if (remainingRadiusSquared < 0) return null

        val zOffset = sqrt(remainingRadiusSquared)
        return chunkCoordinateRange(centerZ - zOffset)..chunkCoordinateRange(centerZ + zOffset)
    }

    fun validate(): SerializableWorldBorder {
        require(centerX.isFinite() && centerZ.isFinite()) { "Border center must be finite" }

        when (effectiveShape) {
            WorldBorderShape.SQUARE, WorldBorderShape.CIRCLE -> {
                val borderRadius = requireNotNull(radius) { "Border radius must be specified." }
                require(borderRadius.isFinite() && borderRadius > 0 && borderRadius <= MAX_RADIUS) {
                    "Border radius is out of range"
                }
                require(width == null && depth == null) { "Square and circular borders cannot have width or depth" }
            }
            WorldBorderShape.RECTANGLE -> {
                val borderWidth = requireNotNull(width) { "Rectangle width must be specified." }
                val borderDepth = requireNotNull(depth) { "Rectangle depth must be specified." }
                require(radius == null) { "Rectangular borders cannot have a radius" }
                require(borderWidth.isFinite() && borderWidth > 0 && borderWidth <= MAX_DIMENSION) {
                    "Rectangle width is out of range"
                }
                require(borderDepth.isFinite() && borderDepth > 0 && borderDepth <= MAX_DIMENSION) {
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
        return this
    }

    companion object {
        const val MAX_RADIUS = 29_999_984.0
        const val MAX_DIMENSION = MAX_RADIUS * 2
        private const val CHUNK_SIZE = 16.0

        private fun chunkCoordinateRange(coordinate: Double) = kotlin.math.floor(coordinate / CHUNK_SIZE).toInt()
    }
}

enum class WorldBorderShape {
    SQUARE,
    CIRCLE,
    RECTANGLE,
}
