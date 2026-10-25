package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WorldBorderListenerTest {
    private val world = mock(World::class.java)
    private val borderRepository = mock(WorldBorderRepository::class.java)
    private val listener = WorldBorderListener(borderRepository)
    private val border =
        SerializableWorldBorder(
            centerX = 0.0,
            centerZ = 0.0,
            radius = 10.0,
            shape = WorldBorderShape.CIRCLE,
        )

    @Test
    fun `clamps player movement to the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val event =
            PlayerMoveEvent(
                mock(Player::class.java),
                Location(world, 9.0, 64.0, 0.0),
                Location(world, 12.0, 64.0, 0.0),
            )

        listener.onPlayerMove(event)

        assertEquals(10.0, event.to?.x)
    }

    @Test
    fun `skips move events without horizontal movement`() {
        val event =
            PlayerMoveEvent(
                mock(Player::class.java),
                Location(world, 9.0, 64.0, 0.0),
                Location(world, 9.0, 65.0, 0.0),
            )

        listener.onPlayerMove(event)

        verify(borderRepository, never()).get(world)
    }

    @Test
    fun `does not custom-check walking against a square border`() {
        whenever(borderRepository.get(world)).thenReturn(border.copy(shape = WorldBorderShape.SQUARE))
        val event =
            PlayerMoveEvent(
                mock(Player::class.java),
                Location(world, 9.0, 64.0, 0.0),
                Location(world, 12.0, 64.0, 0.0),
            )

        listener.onPlayerMove(event)

        assertEquals(12.0, event.to?.x)
    }

    @Test
    fun `clamps rectangular border movement using width and depth`() {
        whenever(borderRepository.get(world)).thenReturn(
            SerializableWorldBorder(
                centerX = 0.0,
                centerZ = 0.0,
                shape = WorldBorderShape.RECTANGLE,
                width = 32.0,
                depth = 16.0,
            ),
        )
        val event =
            PlayerMoveEvent(
                mock(Player::class.java),
                Location(world, 15.0, 64.0, 7.0),
                Location(world, 20.0, 64.0, 12.0),
            )

        listener.onPlayerMove(event)

        assertEquals(16.0, event.to?.x)
        assertEquals(8.0, event.to?.z)
    }

    @Test
    fun `still clamps teleports against a square border`() {
        whenever(borderRepository.get(world)).thenReturn(border.copy(shape = WorldBorderShape.SQUARE))
        val event =
            PlayerTeleportEvent(
                mock(Player::class.java),
                Location(world, 9.0, 64.0, 0.0),
                Location(world, 12.0, 64.0, 0.0),
                PlayerTeleportEvent.TeleportCause.PLUGIN,
            )

        listener.onPlayerMove(event)

        assertEquals(10.0, event.to?.x)
    }

    @Test
    fun `cancels creature spawns outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val creature = mock(Creature::class.java)
        whenever(creature.location).thenReturn(Location(world, 10.1, 64.0, 0.0))
        val event =
            CreatureSpawnEvent(
                creature,
                CreatureSpawnEvent.SpawnReason.NATURAL,
            )

        listener.onCreatureSpawn(event)

        assertTrue(event.isCancelled)
    }
}
