package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import java.util.UUID
import kotlin.random.Random

data class RandomTeleportPosition(
    val x: Double,
    val z: Double,
)

interface RandomTeleportConstraint {
    fun sample(random: Random): RandomTeleportPosition

    fun contains(position: RandomTeleportPosition): Boolean
}

fun interface RandomTeleportConstraintProvider {
    fun constraintFor(worldId: UUID): RandomTeleportConstraint?
}
