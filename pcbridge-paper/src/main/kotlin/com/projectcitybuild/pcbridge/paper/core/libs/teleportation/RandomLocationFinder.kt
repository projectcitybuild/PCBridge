package com.projectcitybuild.pcbridge.paper.core.libs.teleportation

import org.bukkit.Location
import org.bukkit.World

class RandomLocationFinder(
    private val safeYLocationFinder: SafeYLocationFinder,
) {
    fun find(world: World, attempts: Int = 5): Location? {
        for (i in 1..attempts) {
            val coordinate2 = randomCoordinate2(world)
            val safeY = safeYLocationFinder.findY(world, coordinate2.x, coordinate2.z)
            if (safeY != null) {
                return Location(
                    world,
                    coordinate2.x.toDouble(),
                    safeY.toDouble(),
                    coordinate2.z.toDouble()
                )
            }
        }
        return null
    }

    private fun randomCoordinate2(world: World): Coordinate2 {
        val bounds = world.worldBorder
        val center = bounds.center
        val size = bounds.size

        // If a WorldBorder is not set, the API still returns an enormous bounds
        // (60 mil in both axis) so we need to always clamp this
        val xRange = (center.x - size).coerceAtLeast(BORDER_MIN).toInt()..
            (center.x + size).coerceAtMost(BORDER_MAX).toInt()
        val zRange = (center.z - size).coerceAtLeast(BORDER_MIN).toInt()..
            (center.z + size).coerceAtMost(BORDER_MAX).toInt()

        return Coordinate2(
            x = xRange.random(),
            z = zRange.random(),
        )
    }

    private data class Coordinate2(
        val x: Int,
        val z: Int,
    )

    private companion object {
        const val BORDER_MIN = 15_000.0 * -1
        const val BORDER_MAX = 15_000.0
    }
}