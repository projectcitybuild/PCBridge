package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.random.Random

class FindRandomLocationTest {
    @Test
    fun `uses and validates constrained candidates before returning them`() {
        val world = mock<World>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        whenever(world.uid).thenReturn(worldId)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val samples = mutableListOf(
            RandomTeleportPosition(2.0, -2.0),
            RandomTeleportPosition(-2.0, -2.0),
        )
        val constraint = object : RandomTeleportConstraint {
            override fun sample(random: Random): RandomTeleportPosition = samples.removeAt(0)

            override fun contains(position: RandomTeleportPosition): Boolean = position.x < 0
        }
        val provider = RandomTeleportConstraintProvider { requestedWorldId ->
            assertEquals(worldId, requestedWorldId)
            constraint
        }

        val candidates = FindRandomLocation(provider, Random(42)).candidates(player, attempts = 2)

        assertEquals(1, candidates.size)
        assertEquals(-1.5, candidates.single().x)
        assertEquals(-1.5, candidates.single().z)
        assertEquals(0.0, candidates.single().y)
    }

    @Test
    fun `preserves native border sampling when no custom constraint exists`() {
        val world = mock<World>()
        val player = mock<Player>()
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val nativeBorder = mock<WorldBorder>()
        whenever(world.worldBorder).thenReturn(nativeBorder)
        whenever(nativeBorder.center).thenReturn(Location(world, -20.0, 0.0, 40.0))
        whenever(nativeBorder.size).thenReturn(8.0)

        val candidates =
            FindRandomLocation(
                constraintProvider = RandomTeleportConstraintProvider { null },
                random = Random(42),
            ).candidates(player, attempts = 20)

        assertEquals(20, candidates.size)
        assertTrue(candidates.all { it.x in -28.0..-12.0 && it.z in 32.0..48.0 })
    }
}
