package com.projectcitybuild.pcbridge.features.spawns.hooks.listeners

import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.player.PlayerRespawnEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PlayerRespawnListenerTest {
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val listener = PlayerRespawnListener(spawnRepository)

    @Test
    fun `uses configured spawn for respawns without a bed or anchor`() =
        runTest {
            val world = mock(World::class.java)
            val currentLocation = Location(world, 1.0, 64.0, 2.0)
            val spawn = Location(world, 10.0, 70.0, 20.0)
            val event = mock(PlayerRespawnEvent::class.java)
            whenever(event.isBedSpawn).thenReturn(false)
            whenever(event.isAnchorSpawn).thenReturn(false)
            whenever(event.respawnLocation).thenReturn(currentLocation)
            whenever(spawnRepository.get(world)).thenReturn(spawn)

            listener.onPlayerRespawn(event)

            verify(spawnRepository).get(world)
            verify(event).respawnLocation = spawn
        }

    @Test
    fun `preserves respawns caused by a bed or anchor`() =
        runTest {
            val event = mock(PlayerRespawnEvent::class.java)
            whenever(event.isBedSpawn).thenReturn(true)
            whenever(event.isAnchorSpawn).thenReturn(false)

            listener.onPlayerRespawn(event)

            verify(spawnRepository, never()).get(org.mockito.kotlin.any())
        }

    @Test
    fun `preserves respawns caused by an anchor`() =
        runTest {
            val event = mock(PlayerRespawnEvent::class.java)
            whenever(event.isBedSpawn).thenReturn(false)
            whenever(event.isAnchorSpawn).thenReturn(true)

            listener.onPlayerRespawn(event)

            verify(spawnRepository, never()).get(org.mockito.kotlin.any())
        }
}
