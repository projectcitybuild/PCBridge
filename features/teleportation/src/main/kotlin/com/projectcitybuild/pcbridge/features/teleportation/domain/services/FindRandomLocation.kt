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
        require(attempts > 0) { "Candidate count must be positive" }

        val world = player.location.world
        val constraint = constraintProvider.constraintFor(world.uid)

        return buildList {
            val seenPositions = mutableSetOf<RandomTeleportPosition>()
            val sampleLimit = minOf(
                attempts.toLong() * SAMPLE_ATTEMPTS_PER_CANDIDATE,
                MAX_SAMPLE_ATTEMPTS,
            ).toInt()
            var samples = 0

            while (size < attempts && samples < sampleLimit) {
                samples++
                val candidate = randomCoordinateWithinConstraint(constraint)
                    ?: randomCoordinateForPaperBorder(world)

                if (constraint == null) {
                    add(Location(world, candidate.x, 0.0, candidate.z))
                } else if (constraint.contains(candidate) && seenPositions.add(candidate)) {
                    add(Location(world, candidate.x, 0.0, candidate.z))
                }
            }
        }
    }

    private fun randomCoordinateWithinConstraint(constraint: RandomTeleportConstraint?): RandomTeleportPosition? =
        constraint?.sample(random)?.blockCenter()

    private fun randomCoordinateForPaperBorder(world: World): RandomTeleportPosition {
        val worldBorder = world.worldBorder
        val center = worldBorder.center
        val size = worldBorder.size

        // If a world border is not set, the API returns enormous bounds
        // (60 mil in both axis) so we need to always clamp this
        val xRange = clampToBounds(center.x, size)
        val zRange = clampToBounds(center.z, size)

        return RandomTeleportPosition(
            x = xRange.random(random).toDouble(),
            z = zRange.random(random).toDouble(),
        )
    }

    private fun clampToBounds(
        center: Double,
        size: Double,
    ) = IntRange(
        (center - size).coerceAtLeast(BORDER_MIN).toInt(),
        (center + size).coerceAtMost(BORDER_MAX).toInt(),
    )

    private companion object {
        const val BORDER_MAX = 15_000.0
        const val BORDER_MIN = BORDER_MAX * -1
        const val SAMPLE_ATTEMPTS_PER_CANDIDATE = 10L
        const val MAX_SAMPLE_ATTEMPTS = 1_000L
    }
}

private fun RandomTeleportPosition.blockCenter() = RandomTeleportPosition(
    x = floor(x) + 0.5,
    z = floor(z) + 0.5,
)
