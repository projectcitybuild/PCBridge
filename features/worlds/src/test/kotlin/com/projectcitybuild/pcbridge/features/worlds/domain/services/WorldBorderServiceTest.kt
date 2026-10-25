package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderLimits
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.WorldBorder as BukkitWorldBorder
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class WorldBorderServiceTest {
    @Test
    fun `sets native border to the configured bounding square and broadcasts the update`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val border = WorldBorder.Circle(12.5, -8.0, radius = 96.0)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldBorder).thenReturn(platformBorder)
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val service = WorldBorderService(server, repository, broadcaster, mock(SafeYLocationFinder::class.java))

        service.set(world, border)

        verify(repository).set(world, border)
        verify(platformBorder).setCenter(12.5, -8.0)
        verify(platformBorder).setSize(192.0)
        val eventCaptor = argumentCaptor<WorldBorderUpdatedEvent>()
        verify(broadcaster).broadcast(eventCaptor.capture())
        org.junit.jupiter.api.Assertions.assertEquals(border, eventCaptor.firstValue.border)
    }

    @Test
    fun `clears border and broadcasts a null border update`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        whenever(world.worldBorder).thenReturn(platformBorder)
        val service = WorldBorderService(server, repository, broadcaster, mock(SafeYLocationFinder::class.java))

        service.clear(world)

        verify(repository).clear(world)
        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
        val eventCaptor = argumentCaptor<WorldBorderUpdatedEvent>()
        verify(broadcaster).broadcast(eventCaptor.capture())
        org.junit.jupiter.api.Assertions.assertEquals(null, eventCaptor.firstValue.border)
    }
}
