package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import io.papermc.paper.event.entity.EntityMoveEvent
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Creature
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Vehicle
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
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
    private val world = mock(World::class.java).also {
        whenever(it.uid).thenReturn(UUID.randomUUID())
    }
    private val borderRepository = mock(WorldBorderRepository::class.java)
    private val borderService = mock(WorldBorderService::class.java)
    private val safeYLocationFinder = mock(SafeYLocationFinder::class.java)
    private val listener = WorldBorderListener(borderRepository, borderService, safeYLocationFinder)
    private val border = WorldBorder.Circle(
        centerX = 0.0,
        centerZ = 0.0,
        radius = 10.0,
    )

    @Test
    fun `join relocation delegates to the shared safe teleport workflow`() = runTest {
        val player = mock(Player::class.java)
        val event = mock(PlayerJoinEvent::class.java)
        whenever(event.player).thenReturn(player)

        listener.onPlayerJoin(event)

        verify(borderService).relocateIfOutside(player)
    }

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
    fun `pushes back a border crossing when no safe destination exists`() {
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

        assertEquals(9.0, event.to?.x)
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
        verify(safeYLocationFinder, times(1)).findY(world, 9, 0)
    }

    @Test
    fun `invalidates a cached safe y when terrain changes in the cached column`() {
        val playerId = UUID.randomUUID()
        val player = mock(Player::class.java)
        whenever(player.uniqueId).thenReturn(playerId)
        whenever(borderRepository.get(world)).thenReturn(border)
        whenever(safeYLocationFinder.findY(world, 9, 0)).thenReturn(80, 75)
        val firstAttempt = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )
        listener.onPlayerMove(firstAttempt)

        val changedBlock = mock(Block::class.java)
        whenever(changedBlock.world).thenReturn(world)
        whenever(changedBlock.x).thenReturn(9)
        whenever(changedBlock.z).thenReturn(0)
        listener.onBlockBreak(BlockBreakEvent(changedBlock, player))

        val repeatedAttempt = PlayerMoveEvent(
            player,
            Location(world, 9.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )
        listener.onPlayerMove(repeatedAttempt)

        assertEquals(75.0, repeatedAttempt.to?.y)
        verify(safeYLocationFinder, times(2)).findY(world, 9, 0)
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

        listener.onEntitySpawn(event)

        assertTrue(event.isCancelled)
    }

    @Test
    fun `cancels block placement outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val block = mock(Block::class.java)
        whenever(block.world).thenReturn(world)
        whenever(block.x).thenReturn(11)
        whenever(block.z).thenReturn(0)
        val event = mock(BlockPlaceEvent::class.java)
        whenever(event.blockPlaced).thenReturn(block)

        listener.onBlockPlace(event)

        verify(event).setCancelled(true)
    }

    @Test
    fun `cancels block breaking outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val block = mock(Block::class.java)
        whenever(block.world).thenReturn(world)
        whenever(block.x).thenReturn(11)
        whenever(block.z).thenReturn(0)
        val event = BlockBreakEvent(block, mock(Player::class.java))

        listener.onBlockBreak(event)

        assertTrue(event.isCancelled)
    }

    @Test
    fun `cancels bucket fill and empty outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val block = mock(Block::class.java)
        whenever(block.world).thenReturn(world)
        whenever(block.x).thenReturn(11)
        whenever(block.z).thenReturn(0)
        val fillEvent = mock(PlayerBucketFillEvent::class.java)
        val emptyEvent = mock(PlayerBucketEmptyEvent::class.java)
        whenever(fillEvent.block).thenReturn(block)
        whenever(emptyEvent.block).thenReturn(block)

        listener.onBucketFill(fillEvent)
        listener.onBucketEmpty(emptyEvent)

        verify(fillEvent).setCancelled(true)
        verify(emptyEvent).setCancelled(true)
    }

    @Test
    fun `cancels fluid flow only when crossing the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val source = mock(Block::class.java)
        val destination = mock(Block::class.java)
        whenever(source.world).thenReturn(world)
        whenever(source.x).thenReturn(9)
        whenever(source.z).thenReturn(0)
        whenever(destination.world).thenReturn(world)
        whenever(destination.x).thenReturn(10)
        whenever(destination.z).thenReturn(0)
        val event = mock(BlockFromToEvent::class.java)
        whenever(event.block).thenReturn(source)
        whenever(event.toBlock).thenReturn(destination)

        listener.onFluidFlow(event)

        verify(event).setCancelled(true)
    }

    @Test
    fun `cancels piston extension that would move a block outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val piston = mock(Block::class.java)
        val moved = mock(Block::class.java)
        val destination = mock(Block::class.java)
        whenever(piston.world).thenReturn(world)
        whenever(piston.x).thenReturn(8)
        whenever(piston.z).thenReturn(0)
        whenever(piston.getRelative(BlockFace.EAST)).thenReturn(moved)
        whenever(moved.world).thenReturn(world)
        whenever(moved.x).thenReturn(9)
        whenever(moved.z).thenReturn(0)
        whenever(moved.getRelative(BlockFace.EAST)).thenReturn(destination)
        whenever(destination.world).thenReturn(world)
        whenever(destination.x).thenReturn(10)
        whenever(destination.z).thenReturn(0)
        val event = mock(BlockPistonExtendEvent::class.java)
        whenever(event.block).thenReturn(piston)
        whenever(event.direction).thenReturn(BlockFace.EAST)
        whenever(event.blocks).thenReturn(listOf(moved))

        listener.onPistonExtend(event)

        verify(event).setCancelled(true)
    }

    @Test
    fun `constrains living entities already outside the configured border when they move`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val creature = mock(Creature::class.java)
        val event = EntityMoveEvent(
            creature,
            Location(world, 11.0, 64.0, 0.0),
            Location(world, 12.0, 64.0, 0.0),
        )

        listener.onLivingEntityMove(event)

        assertTrue(border.geometry.contains(event.to.x, event.to.z))
    }

    @Test
    fun `clamps non-player entity teleports to the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val entity = mock(Entity::class.java)
        val event = mock(EntityTeleportEvent::class.java)
        whenever(event.entity).thenReturn(entity)
        whenever(event.to).thenReturn(Location(world, 12.0, 64.0, 0.0))
        val destinationCaptor = org.mockito.kotlin.argumentCaptor<Location>()

        listener.onEntityTeleport(event)

        verify(event).setTo(destinationCaptor.capture())
        assertEquals(10.0 - 0.3 * kotlin.math.sqrt(2.0), destinationCaptor.firstValue.x, 1e-9)
    }

    @Test
    fun `returns vehicles to the last in-border location`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val vehicle = mock(Vehicle::class.java)
        val from = Location(world, 9.0, 64.0, 0.0)
        val event = mock(VehicleMoveEvent::class.java)
        whenever(event.vehicle).thenReturn(vehicle)
        whenever(event.from).thenReturn(from)
        whenever(event.to).thenReturn(Location(world, 11.0, 64.0, 0.0))

        listener.onVehicleMove(event)

        verify(vehicle).teleport(from)
    }

    @Test
    fun `cancels projectiles launched outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val projectile = mock(Projectile::class.java)
        whenever(projectile.location).thenReturn(Location(world, 11.0, 64.0, 0.0))
        val event = mock(ProjectileLaunchEvent::class.java)
        whenever(event.entity).thenReturn(projectile)

        listener.onProjectileLaunch(event)

        verify(event).setCancelled(true)
    }

    @Test
    fun `cancels projectile hits outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val projectile = mock(Projectile::class.java)
        whenever(projectile.location).thenReturn(Location(world, 11.0, 64.0, 0.0))
        val event = mock(ProjectileHitEvent::class.java)
        whenever(event.entity).thenReturn(projectile)

        listener.onProjectileHit(event)

        verify(event).setCancelled(true)
    }

    @Test
    fun `removes out-of-border blocks from entity explosions`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val inside = mock(Block::class.java)
        val outside = mock(Block::class.java)
        whenever(inside.world).thenReturn(world)
        whenever(outside.world).thenReturn(world)
        whenever(inside.x).thenReturn(0)
        whenever(inside.z).thenReturn(0)
        whenever(outside.x).thenReturn(11)
        whenever(outside.z).thenReturn(0)
        val blocks = mutableListOf(inside, outside)
        val event = mock(EntityExplodeEvent::class.java)
        whenever(event.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        whenever(event.blockList()).thenReturn(blocks)

        listener.onEntityExplode(event)

        assertEquals(listOf(inside), blocks)
    }

    @Test
    fun `cancels block explosions originating outside the configured border`() {
        whenever(borderRepository.get(world)).thenReturn(border)
        val origin = mock(Block::class.java)
        whenever(origin.world).thenReturn(world)
        whenever(origin.x).thenReturn(11)
        whenever(origin.z).thenReturn(0)
        val event = mock(BlockExplodeEvent::class.java)
        whenever(event.block).thenReturn(origin)

        listener.onBlockExplode(event)

        verify(event).setCancelled(true)
    }
}
