package com.projectcitybuild.pcbridge.features.portals.hooks.listeners

import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLinkType
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.portals.domain.repositories.PortalLinksRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.logging.Logger

class PortalListenerTest {
    @Test
    fun `creates an End platform nearby without replacing occupied blocks`() {
        val world = mock(World::class.java)
        val spawn = mock(Location::class.java)
        val border = mock(WorldBorder::class.java)
        val blocks = mutableMapOf<Triple<Int, Int, Int>, Block>()
        whenever(world.environment).thenReturn(World.Environment.THE_END)
        whenever(world.spawnLocation).thenReturn(spawn)
        whenever(spawn.blockX).thenReturn(0)
        whenever(spawn.blockY).thenReturn(65)
        whenever(spawn.blockZ).thenReturn(0)
        whenever(spawn.yaw).thenReturn(90f)
        whenever(spawn.pitch).thenReturn(10f)
        whenever(world.minHeight).thenReturn(0)
        whenever(world.maxHeight).thenReturn(256)
        whenever(world.worldBorder).thenReturn(border)
        whenever(border.isInside(any<Location>())).thenReturn(true)
        whenever(world.isChunkLoaded(any(), any())).thenReturn(true)
        whenever(world.getBlockAt(any<Int>(), any<Int>(), any<Int>())).thenAnswer { invocation ->
            val x = invocation.getArgument<Int>(0)
            val y = invocation.getArgument<Int>(1)
            val z = invocation.getArgument<Int>(2)
            mock(Block::class.java).also { block ->
                val type = if (x == 0 && y == 64 && z == 0) Material.STONE else Material.AIR
                whenever(block.type).thenReturn(type)
                blocks[Triple(x, y, z)] = block
            }
        }
        val listener = PortalListener(
            plugin = mock(Plugin::class.java),
            server = mock(Server::class.java),
            portalLinksRepository = mock(PortalLinksRepository::class.java),
            worldLifecycleService = mock(WorldLifecycleService::class.java),
            settings = PortalSettings(),
        )

        val destination = listener.prepareEndPlatform(world)

        assertNotNull(destination)
        assertEquals(-2.5, destination!!.x)
        assertEquals(65.0, destination.y)
        assertEquals(-2.5, destination.z)
        assertEquals(90f, destination.yaw)
        assertEquals(10f, destination.pitch)
        verify(blocks.getValue(Triple(0, 64, 0)), never()).setType(Material.OBSIDIAN, false)
        verify(blocks.getValue(Triple(-5, 64, -5))).setType(Material.OBSIDIAN, false)
    }

    @Test
    fun `cancels and logs travel when no destination is registered`() {
        val plugin = mock(Plugin::class.java)
        val logger = mock(Logger::class.java)
        val portalLinksRepository = mock(PortalLinksRepository::class.java)
        val sourceWorld = mock(World::class.java)
        val from = mock(Location::class.java)
        val player = mock(Player::class.java)
        val event = mock(PlayerPortalEvent::class.java)
        whenever(plugin.logger).thenReturn(logger)
        whenever(sourceWorld.name).thenReturn("world")
        whenever(sourceWorld.environment).thenReturn(World.Environment.NORMAL)
        whenever(from.world).thenReturn(sourceWorld)
        whenever(event.cause).thenReturn(TeleportCause.NETHER_PORTAL)
        whenever(event.from).thenReturn(from)
        whenever(event.player).thenReturn(player)
        whenever(portalLinksRepository.resolve("world", "NORMAL", PortalLinkType.NETHER))
            .thenReturn(null)
        val listener = PortalListener(
            plugin = plugin,
            server = mock(Server::class.java),
            portalLinksRepository = portalLinksRepository,
            worldLifecycleService = mock(WorldLifecycleService::class.java),
            settings = PortalSettings(sendNoDestinationMessage = false),
        )

        listener.onPlayerPortal(event)

        verify(event).setCancelled(true)
        verify(logger).warning("No nether portal destination is registered for world")
    }

    @Test
    fun `nether coordinate scaling floors negative coordinates and preserves orientation`() {
        val sourceWorld = mock(World::class.java)
        val destinationWorld = mock(World::class.java)
        val from = mock(Location::class.java)
        val currentTo = mock(Location::class.java)
        val border = mock(WorldBorder::class.java)
        whenever(from.world).thenReturn(sourceWorld)
        whenever(from.x).thenReturn(-8.2)
        whenever(from.z).thenReturn(-17.9)
        whenever(sourceWorld.coordinateScale).thenReturn(1.0)
        whenever(destinationWorld.coordinateScale).thenReturn(8.0)
        whenever(destinationWorld.minHeight).thenReturn(0)
        whenever(destinationWorld.maxHeight).thenReturn(256)
        whenever(destinationWorld.worldBorder).thenReturn(border)
        whenever(border.isInside(any<Location>())).thenReturn(true)
        whenever(currentTo.y).thenReturn(70.0)
        whenever(currentTo.yaw).thenReturn(45f)
        whenever(currentTo.pitch).thenReturn(15f)
        val listener = PortalListener(
            plugin = mock(Plugin::class.java),
            server = mock(Server::class.java),
            portalLinksRepository = mock(PortalLinksRepository::class.java),
            worldLifecycleService = mock(WorldLifecycleService::class.java),
            settings = PortalSettings(),
        )

        val destination = listener.destinationLocation(
            entity = mock(Player::class.java),
            from = from,
            currentTo = currentTo,
            destinationWorld = destinationWorld,
            type = PortalLinkType.NETHER,
        )

        assertEquals(destinationWorld, destination.world)
        assertEquals(-2.0, destination.x)
        assertEquals(-3.0, destination.z)
        assertEquals(70.0, destination.y)
        assertEquals(45f, destination.yaw)
        assertEquals(15f, destination.pitch)
    }

    @Test
    fun `rejects scaled coordinates outside the supported block range`() {
        val sourceWorld = mock(World::class.java)
        val destinationWorld = mock(World::class.java)
        val from = mock(Location::class.java)
        whenever(from.world).thenReturn(sourceWorld)
        whenever(from.x).thenReturn(Int.MAX_VALUE.toDouble() * 2)
        whenever(from.z).thenReturn(0.0)
        whenever(sourceWorld.coordinateScale).thenReturn(1.0)
        whenever(destinationWorld.coordinateScale).thenReturn(1.0)
        whenever(destinationWorld.minHeight).thenReturn(0)
        whenever(destinationWorld.maxHeight).thenReturn(256)

        val listener = PortalListener(
            plugin = mock(Plugin::class.java),
            server = mock(Server::class.java),
            portalLinksRepository = mock(PortalLinksRepository::class.java),
            worldLifecycleService = mock(WorldLifecycleService::class.java),
            settings = PortalSettings(),
        )

        val failure = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            listener.destinationLocation(
                entity = mock(Player::class.java),
                from = from,
                currentTo = null,
                destinationWorld = destinationWorld,
                type = PortalLinkType.NETHER,
            )
        }

        assertEquals("Portal destination coordinates exceed the supported range", failure.message)
    }

    @Test
    fun `rejects destinations outside the target world border`() {
        val sourceWorld = mock(World::class.java)
        val destinationWorld = mock(World::class.java)
        val from = mock(Location::class.java)
        val border = mock(WorldBorder::class.java)
        whenever(from.world).thenReturn(sourceWorld)
        whenever(from.x).thenReturn(1.0)
        whenever(from.z).thenReturn(1.0)
        whenever(sourceWorld.coordinateScale).thenReturn(1.0)
        whenever(destinationWorld.coordinateScale).thenReturn(1.0)
        whenever(destinationWorld.minHeight).thenReturn(0)
        whenever(destinationWorld.maxHeight).thenReturn(256)
        whenever(destinationWorld.worldBorder).thenReturn(border)
        whenever(border.isInside(any<Location>())).thenReturn(false)

        val listener = PortalListener(
            plugin = mock(Plugin::class.java),
            server = mock(Server::class.java),
            portalLinksRepository = mock(PortalLinksRepository::class.java),
            worldLifecycleService = mock(WorldLifecycleService::class.java),
            settings = PortalSettings(),
        )

        val failure = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            listener.destinationLocation(
                entity = mock(Player::class.java),
                from = from,
                currentTo = null,
                destinationWorld = destinationWorld,
                type = PortalLinkType.NETHER,
            )
        }

        assertEquals("Portal destination is outside the world border", failure.message)
    }
}
