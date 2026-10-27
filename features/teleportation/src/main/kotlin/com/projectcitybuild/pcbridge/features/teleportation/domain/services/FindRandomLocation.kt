package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import kotlin.math.floor
import kotlin.random.Random

class FindRandomLocation(
    private val constraintProvider: RandomTeleportConstraintProvider,
    private val random: Random = Random.Default,
) {
    fun candidates(
        player: Player,
        attempts: Int = 5,
    ): List<Location> {
        val world = player.location.world
        val constraint = constraintProvider.constraintFor(world.uid)

        return buildList {
            repeat(attempts) {
                val candidate = if (constraint == null) {
                    randomCoordinate2(world)
                } else {
                    blockCenter(constraint.sample(random))
                }
                if (constraint == null || constraint.contains(candidate)) {
                    add(Location(world, candidate.x, 0.0, candidate.z))
                }
            }
        }
    }

    private fun blockCenter(position: RandomTeleportPosition) = RandomTeleportPosition(
        floor(position.x) + 0.5,
        floor(position.z) + 0.5,
    )

    private fun randomCoordinate2(world: World): RandomTeleportPosition {
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

        return RandomTeleportPosition(
            x = xRange.random(random).toDouble(),
            z = zRange.random(random).toDouble(),
        )
    }

    private companion object {
        const val BORDER_MAX = 15_000.0
        const val BORDER_MIN = BORDER_MAX * -1
    }
}
