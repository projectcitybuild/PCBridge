package com.projectcitybuild.pcbridge.features.spawns.hooks.listeners

import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PlayerFirstJoinSpawnListenerTest {
    private val playerTeleporter = mock(PlayerTeleporter::class.java)
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val listener = PlayerFirstJoinSpawnListener(playerTeleporter, spawnRepository)

    @Test
    fun `does not move returning players to spawn`() =
        runTest {
            val player = mock(Player::class.java)
            whenever(player.hasPlayedBefore()).thenReturn(true)
            val event = mock(PlayerJoinEvent::class.java)
            whenever(event.player).thenReturn(player)

            listener.onPlayerJoin(event)

            verify(spawnRepository, never()).get(org.mockito.kotlin.any())
            verify(playerTeleporter, never()).move(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            )
        }

    @Test
    fun `teleports a first-time player to the saved spawn`() =
        runTest {
            val player = mock(Player::class.java)
            val world = mock(World::class.java)
            val spawn = Location(world, 10.0, 64.0, -5.0)
            whenever(player.hasPlayedBefore()).thenReturn(false)
            whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
            val event = mock(PlayerJoinEvent::class.java)
            whenever(event.player).thenReturn(player)
            whenever(spawnRepository.get(world)).thenReturn(spawn)

            listener.onPlayerJoin(event)

            verify(spawnRepository).get(world)
            verify(playerTeleporter).move(
                player,
                spawn,
                PlayerTeleportEvent.TeleportCause.COMMAND,
                PlayerTeleporter.TeleportOptions(),
            )
        }
}
