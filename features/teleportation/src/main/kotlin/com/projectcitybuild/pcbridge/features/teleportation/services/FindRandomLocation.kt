package com.projectcitybuild.pcbridge.features.teleportation.services

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class FindRandomLocation {
    fun candidates(
        player: Player,
        attempts: Int = 5,
    ): List<Location> {
        val world = player.location.world
        return List(attempts) {
            val coordinate2 = randomCoordinate2(world)
            Location(
                world,
                coordinate2.x.toDouble(),
                0.0,
                coordinate2.z.toDouble(),
            )
        }
    }

    private fun randomCoordinate2(world: World): Coordinate2 {
        val bounds = world.worldBorder
        val center = bounds.center
        val size = bounds.size

        // If a WorldBorder is not set, the API still returns an enormous bounds
        // (60 mil in both axis) so we need to always clamp this
        val xRange =
            IntRange(
                (center.x - size).coerceAtLeast(BORDER_MIN).toInt(),
                (center.x + size).coerceAtMost(BORDER_MAX).toInt(),
            )
        val zRange =
            IntRange(
                (center.z - size).coerceAtLeast(BORDER_MIN).toInt(),
                (center.z + size).coerceAtMost(BORDER_MAX).toInt(),
            )

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
        const val BORDER_MAX = 15_000.0
        const val BORDER_MIN = BORDER_MAX * -1
    }
}
