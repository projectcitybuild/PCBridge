package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class WorldBorderListenerTest {
    private val world = mock(World::class.java)
    private val borderRepository = mock(WorldBorderRepository::class.java)
    private val safeYLocationFinder = mock(SafeYLocationFinder::class.java)
    private val listener = WorldBorderListener(borderRepository, safeYLocationFinder)
    private val border = WorldBorder.Circle(
        centerX = 0.0,
        centerZ = 0.0,
        radius = 10.0,
    )

    @Test
    fun `clamps player movement to the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(80)

        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())

        val event = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )

        listener.onPlayerMove(event)

        assertEquals(10.0 - 0.3 * kotlin.math.sqrt(2.0), requireNotNull(event.to).x, 1e-9)
        assertEquals(80.0, event.to?.y)
        verify(player).sendMessage(l10n.worlds.borderReached)
        verify(player).playSound(event.from, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @Test
    fun `skips move events without horizontal movement`() {
        val event = PlayerMoveEvent(
            mock(Player::class.java),
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 9.0, 65.0, 0.0),
        )

        listener.onPlayerMove(event)

        verify(borderRepository, never()).get(world)
    }

    @Test
    fun `clamps square border movement and notifies the player`() {
        whenever(borderRepository.get(world)).thenReturn(WorldBorder.Square(0.0, 0.0, 10.0))
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(80)

        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())

        val event = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )

        listener.onPlayerMove(event)

        assertEquals(9.7, event.to?.x)
        verify(player).sendMessage(l10n.worlds.borderReached)
        verify(player).playSound(event.from, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @Test
    fun `clamps rectangular border movement using width and depth`() {
        whenever(borderRepository.get(world)).thenReturn(
            WorldBorder.Rectangle(
                centerX = 0.0,
                centerZ = 0.0,
                width = 32.0,
                depth = 16.0,
            ),
        )
        whenever(safeYLocationFinder.findY(world, 15, 7)).thenReturn(80)
        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        val event = PlayerMoveEvent(
            player,
            Location(world, 15.0, 64.0, 7.0),
            Location(world, 20.0, 64.0, 12.0),
        )

        listener.onPlayerMove(event)

        assertEquals(15.7, event.to?.x)
        assertEquals(7.7, event.to?.z)
        assertEquals(80.0, event.to?.y)
    }

    @Test
    fun `clamps teleport events against a square border`() {
        whenever(borderRepository.get(world)).thenReturn(WorldBorder.Square(0.0, 0.0, 10.0))
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(80)
        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        val event = PlayerTeleportEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
            PlayerTeleportEvent.TeleportCause.PLUGIN,
        )

        listener.onPlayerTeleport(event)

        assertEquals(9.7, event.to?.x)
        assertEquals(80.0, event.to?.y)
    }

    @Test
    fun `cancels border crossing when no safe destination exists`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(null)
        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        val event = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )

        listener.onPlayerMove(event)

        assertTrue(event.isCancelled)
        verify(player).sendMessage(l10n.worlds.borderReached)
        verify(player).playSound(event.from, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @Test
    fun `throttles border feedback while the player keeps pushing against the border`() {
        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(borderRepository.get(world)).thenReturn(border)
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(80)
        val firstAttempt = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )
        val repeatedAttempt = PlayerMoveEvent(
            player,
            Location(world, 9.7, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )

        listener.onPlayerMove(firstAttempt)
        listener.onPlayerMove(repeatedAttempt)

        verify(player, times(1)).sendMessage(l10n.worlds.borderReached)
        verify(player, times(1)).playSound(firstAttempt.from, "minecraft:entity.player.teleport", 1F, 1F)
    }

    @Test
    fun `cancels creature spawns outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val creature = mock(Creature::class.java)
        whenever(creature.location).thenReturn(Location(world, 10.1, 64.0, 0.0))
        val event = CreatureSpawnEvent(
            creature,
            CreatureSpawnEvent.SpawnReason.NATURAL,
        )

        listener.onCreatureSpawn(event)

        assertTrue(event.isCancelled)
    }
}
