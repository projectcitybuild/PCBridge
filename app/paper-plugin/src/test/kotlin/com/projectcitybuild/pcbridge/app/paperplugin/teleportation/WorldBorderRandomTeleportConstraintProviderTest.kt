package com.projectcitybuild.pcbridge.app.paperplugin.teleportation

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.FindRandomLocation
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.random.Random

class WorldBorderRandomTeleportConstraintProviderTest {
    @Test
    fun `teleportation candidates use the configured rectangular border`() {
        val server = mock<Server>()
        val world = mock<World>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Rectangle(
            centerX = -20.0,
            centerZ = 10.0,
            width = 8.0,
            depth = 4.0,
        )
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val repository = mock<WorldBorderRepository>()
        whenever(repository.get(world)).thenReturn(border)
        val provider = WorldBorderRandomTeleportConstraintProvider(server, repository)

        val candidates = FindRandomLocation(provider, Random(42)).candidates(player, attempts = 100)

        assertEquals(100, candidates.size)
        assertTrue(
            candidates.all { candidate ->
                candidate.x in -24.0..-16.0 &&
                    candidate.z in 8.0..12.0 &&
                    border.geometry.contains(candidate.x, candidate.z)
            },
        )
    }
}
