package com.projectcitybuild.pcbridge.paper.features.borders.data

import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
sealed class Border {
    abstract fun contains(x: Double, z: Double): Boolean

    fun contains(location: Location): Boolean
        = contains(location.x, location.z)

    abstract fun clamp(location: Location): Location

    @Serializable
    data class Rectangle(
        val minX: Double,
        val minZ: Double,
        val maxX: Double,
        val maxZ: Double,
    ): Border() {
        override fun contains(x: Double, z: Double): Boolean {
            return x > minX
                && x < maxX
                && z > minZ
                && z < maxZ
        }

        override fun clamp(location: Location): Location {
            val knockback = 2

            return location.clone().apply {
                if (x < minX) x = minX + knockback
                else if (x > maxX) x = maxX - knockback

                if (z < minZ) z = minZ + knockback
                else if (z > maxZ) z = maxZ - knockback
            }
        }
    }

    @Serializable
    data class Circle(
        val radius: Double,
        val centerX: Double,
        val centerZ: Double,
    ): Border() {
        override fun contains(x: Double, z: Double): Boolean {
            // Pythagoras theorem to get the distance from the center (a² + b² = c²)
            val dx = centerX - x
            val dz = centerZ - z
            val squaredDistance = dx * dx + dz + dz

            // Avoid square root due to compute cost.
            // Since we're only performing a comparison (i.e. we don't need the exact distance)
            // this is mathematically equivalent to squaring the radius
            return squaredDistance < radius * radius
        }

        override fun clamp(location: Location): Location {
            // TODO
            return location.clone()
        }
    }
}