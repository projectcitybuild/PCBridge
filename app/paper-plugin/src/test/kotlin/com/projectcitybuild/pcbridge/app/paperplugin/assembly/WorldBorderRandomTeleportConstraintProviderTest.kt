package com.projectcitybuild.pcbridge.app.paperplugin.assembly

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
import kotlin.math.floor
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

        assertEquals(32, candidates.size)
        assertEquals(32, candidates.map { it.x to it.z }.distinct().size)
        assertTrue(
            candidates.all { candidate ->
                candidate.x in -24.0..-16.0 &&
                    candidate.z in 8.0..12.0 &&
                    border.geometry.contains(candidate.x, candidate.z)
            },
        )
    }

    @Test
    fun `teleportation candidates are block centered and valid inside circles`() {
        val server = mock<Server>()
        val world = mock<World>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Circle(
            centerX = -20.25,
            centerZ = 10.75,
            radius = 8.0,
        )
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val repository = mock<WorldBorderRepository>()
        whenever(repository.get(world)).thenReturn(border)

        val candidates = FindRandomLocation(
            WorldBorderRandomTeleportConstraintProvider(server, repository),
            Random(42),
        ).candidates(player, attempts = 5)

        assertEquals(5, candidates.size)
        assertTrue(candidates.all { it.x == floor(it.x) + 0.5 && it.z == floor(it.z) + 0.5 })
        assertTrue(candidates.all { border.geometry.contains(it.x, it.z) })
    }

    @Test
    fun `fractional rectangle returns its only valid block center when fewer than requested exist`() {
        val server = mock<Server>()
        val world = mock<World>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Rectangle(
            centerX = 0.25,
            centerZ = 0.25,
            width = 1.0,
            depth = 1.0,
        )
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val repository = mock<WorldBorderRepository>()
        whenever(repository.get(world)).thenReturn(border)

        val candidates = FindRandomLocation(
            WorldBorderRandomTeleportConstraintProvider(server, repository),
            Random(42),
        ).candidates(player, attempts = 5)

        assertEquals(1, candidates.size)
        assertEquals(0.5, candidates.single().x)
        assertEquals(0.5, candidates.single().z)
        assertTrue(border.geometry.contains(candidates.single().x, candidates.single().z))
    }

    @Test
    fun `small circle returns only available block centers when fewer than requested exist`() {
        val server = mock<Server>()
        val world = mock<World>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Circle(
            centerX = 0.0,
            centerZ = 0.0,
            radius = 0.75,
        )
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val repository = mock<WorldBorderRepository>()
        whenever(repository.get(world)).thenReturn(border)

        val candidates = FindRandomLocation(
            WorldBorderRandomTeleportConstraintProvider(server, repository),
            Random(42),
        ).candidates(player, attempts = 5)

        assertEquals(4, candidates.size)
        assertEquals(4, candidates.map { it.x to it.z }.distinct().size)
        assertTrue(candidates.all { border.geometry.contains(it.x, it.z) })
    }
}
