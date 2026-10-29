package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderLimits
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.WorldBorder as BukkitWorldBorder
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import java.util.concurrent.CompletableFuture

class WorldBorderServiceTest {
    @Test
    fun `loading absent border restores the native default after a stale border`() {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        stubNativeBorder(world, platformBorder, Location(world, 128.0, 64.0, -256.0), 512.0)
        whenever(repository.get(world)).thenReturn(null)
        val service = WorldBorderService(
            server,
            repository,
            mock(SpigotEventBroadcaster::class.java),
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        assertEquals(null, service.load(world))

        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
    }

    @Test
    fun `loading non-square rectangle restores the default native border`() {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val border = WorldBorder.Rectangle(12.0, -8.0, width = 64.0, depth = 32.0)
        stubNativeBorder(world, platformBorder)
        whenever(repository.get(world)).thenReturn(border)
        val service = WorldBorderService(
            server,
            repository,
            mock(SpigotEventBroadcaster::class.java),
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        assertEquals(border, service.load(world))

        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
    }

    @Test
    fun `setting non-square rectangle restores the default native border`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val border = WorldBorder.Rectangle(12.0, -8.0, width = 64.0, depth = 32.0)
        stubNativeBorder(world, platformBorder)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val service = WorldBorderService(
            server,
            repository,
            mock(SpigotEventBroadcaster::class.java),
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        service.set(world, border)

        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
        verify(repository).set(world, border)
    }

    @Test
    fun `setting a circle restores the default native border and broadcasts the update`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val border = WorldBorder.Circle(12.5, -8.0, radius = 96.0)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        stubNativeBorder(world, platformBorder)
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        service.set(world, border)

        verify(repository).set(world, border)
        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
        val eventCaptor = argumentCaptor<WorldBorderUpdatedEvent>()
        verify(broadcaster).broadcast(eventCaptor.capture())
        assertEquals(border, eventCaptor.firstValue.border)

        val ordered = inOrder(platformBorder, repository, broadcaster)
        ordered.verify(platformBorder).setCenter(0.0, 0.0)
        ordered.verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
        ordered.verify(repository).set(world, border)
        ordered.verify(broadcaster).broadcast(any<WorldBorderUpdatedEvent>())
    }

    @Test
    fun `setting a square applies its native border`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val border = WorldBorder.Square(12.5, -8.0, radius = 96.0)
        stubNativeBorder(world, platformBorder)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val service = WorldBorderService(
            server,
            repository,
            mock(SpigotEventBroadcaster::class.java),
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        service.set(world, border)

        verify(platformBorder).setCenter(12.5, -8.0)
        verify(platformBorder).setSize(192.0)
        verify(repository).set(world, border)
    }

    @Test
    fun `clears border and broadcasts a null border update`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        stubNativeBorder(world, platformBorder)
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        service.clear(world)

        verify(repository).clear(world)
        verify(platformBorder).setCenter(0.0, 0.0)
        verify(platformBorder).setSize(WorldBorderLimits.MAX_DIMENSION)
        val eventCaptor = argumentCaptor<WorldBorderUpdatedEvent>()
        verify(broadcaster).broadcast(eventCaptor.capture())
        assertEquals(null, eventCaptor.firstValue.border)
    }

    @Test
    fun `relocates without requiring safe ground and validates the current border`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val playerTeleporter = mock(PlayerTeleporter::class.java)
        val player = mock(Player::class.java)
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Square(0.0, 0.0, radius = 10.0)
        whenever(world.uid).thenReturn(worldId)
        stubNativeBorder(world, platformBorder)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(server.onlinePlayers).thenReturn(listOf(player))
        whenever(repository.get(world)).thenReturn(border)
        whenever(player.world).thenReturn(world)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.location).thenReturn(
            Location(world, 12.0, 64.0, 0.0),
            Location(world, 9.5, 65.0, 0.0),
        )
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            playerTeleporter,
            Dispatchers.Unconfined,
        )

        service.set(world, border)

        val destination = argumentCaptor<Location>()
        val validator = argumentCaptor<(Location) -> Boolean>()
        verify(playerTeleporter).moveValidated(
            eq(player),
            destination.capture(),
            eq(TeleportCause.PLUGIN),
            eq(
                PlayerTeleporter.TeleportOptions(
                    adjustYForSafety = false,
                    playSound = false,
                    recordHistory = false,
                ),
            ),
            validator.capture(),
        )
        assertTrue(border.geometry.contains(destination.firstValue.x, destination.firstValue.z))
        assertEquals(64.0, destination.firstValue.y)

        whenever(repository.get(world)).thenReturn(WorldBorder.Circle(0.0, 0.0, radius = 5.0))
        assertFalse(validator.firstValue(destination.firstValue))
    }

    @Test
    fun `persistence failure restores native border and does not broadcast`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val border = WorldBorder.Square(12.5, -8.0, radius = 96.0)
        stubNativeBorder(world, platformBorder, Location(world, 7.0, 64.0, -3.0), 128.0)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val persistenceFailure = IllegalStateException("disk unavailable")
        doThrow(persistenceFailure).whenever(repository).set(world, border)
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        var caught: Throwable? = null
        try {
            service.set(world, border)
        } catch (failure: Throwable) {
            caught = failure
        }

        assertEquals(persistenceFailure.message, caught?.message)
        verify(platformBorder).setCenter(12.5, -8.0)
        verify(platformBorder).setSize(192.0)
        verify(platformBorder).setCenter(7.0, -3.0)
        verify(platformBorder).setSize(128.0)
        verify(broadcaster, never()).broadcast(any<Event>())
    }

    @Test
    fun `native border failure restores prior state without persisting or broadcasting`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val border = WorldBorder.Square(12.5, -8.0, radius = 96.0)
        val nativeFailure = IllegalStateException("native border unavailable")
        stubNativeBorder(world, platformBorder, Location(world, 7.0, 64.0, -3.0), 128.0)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(server.onlinePlayers).thenReturn(emptyList())
        doAnswer { invocation ->
            if (invocation.getArgument<Double>(0) == 192.0) throw nativeFailure
            null
        }.whenever(platformBorder).setSize(any<Double>())
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            mock(PlayerTeleporter::class.java),
            Dispatchers.Unconfined,
        )

        var caught: Throwable? = null
        try {
            service.set(world, border)
        } catch (failure: Throwable) {
            caught = failure
        }

        assertEquals(nativeFailure.message, caught?.message)
        verify(repository, never()).set(world, border)
        verify(platformBorder).setCenter(12.5, -8.0)
        verify(platformBorder).setCenter(7.0, -3.0)
        verify(platformBorder).setSize(128.0)
        verify(broadcaster, never()).broadcast(any<Event>())
    }

    @Test
    fun `one failed relocation does not prevent other players and committed update is broadcast`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Square(0.0, 0.0, radius = 10.0)
        val firstPlayer = mock(Player::class.java)
        val secondPlayer = mock(Player::class.java)
        val firstPlayerId = UUID.randomUUID()
        val secondPlayerId = UUID.randomUUID()
        stubNativeBorder(world, platformBorder)
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(server.onlinePlayers).thenReturn(listOf(firstPlayer, secondPlayer))
        whenever(repository.get(world)).thenReturn(border)
        whenever(firstPlayer.uniqueId).thenReturn(firstPlayerId)
        whenever(firstPlayer.name).thenReturn("first")
        whenever(firstPlayer.world).thenReturn(world)
        whenever(firstPlayer.isOnline).thenReturn(true)
        whenever(firstPlayer.location).thenReturn(Location(world, 15.0, 64.0, 0.0))
        whenever(secondPlayer.uniqueId).thenReturn(secondPlayerId)
        whenever(secondPlayer.name).thenReturn("second")
        whenever(secondPlayer.world).thenReturn(world)
        whenever(secondPlayer.isOnline).thenReturn(true)
        var secondLocation = Location(world, 15.0, 64.0, 0.0)
        whenever(secondPlayer.location).thenAnswer { secondLocation.clone() }

        val safeYLocationFinder = mock(SafeYLocationFinder::class.java)
        val playerTeleporter = PlayerTeleporter(
            safeYLocationFinder,
            mock(TeleportHistoryStorage::class.java),
            server,
            Dispatchers.Unconfined,
        )
        whenever(world.getChunkAtAsyncUrgently(any<Location>()))
            .thenReturn(CompletableFuture.completedFuture(mock(Chunk::class.java)))
        whenever(world.isChunkLoaded(any(), any())).thenReturn(true)
        whenever(firstPlayer.teleportAsync(any<Location>(), any<TeleportCause>()))
            .thenReturn(CompletableFuture.completedFuture(false))
        whenever(secondPlayer.teleportAsync(any<Location>(), any<TeleportCause>())).thenAnswer { invocation ->
            secondLocation = invocation.getArgument<Location>(0)
            CompletableFuture.completedFuture(true)
        }
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            playerTeleporter,
            Dispatchers.Unconfined,
        )

        val failures = service.set(world, border)

        assertEquals(listOf(firstPlayerId), failures)
        assertTrue(border.geometry.contains(secondLocation.x, secondLocation.z))
        assertEquals(64.0, secondLocation.y)
        verify(secondPlayer).teleportAsync(any<Location>(), eq(TeleportCause.PLUGIN))
        verify(broadcaster).broadcast(any<WorldBorderUpdatedEvent>())
    }

    @Test
    fun `caller cancellation after commit still broadcasts update`() = runTest {
        val world = mock(World::class.java)
        val platformBorder = mock(BukkitWorldBorder::class.java)
        val server = mock(Server::class.java)
        val repository = mock(WorldBorderRepository::class.java)
        val broadcaster = mock(SpigotEventBroadcaster::class.java)
        val player = mock(Player::class.java)
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Square(0.0, 0.0, radius = 10.0)
        val pendingChunkLoad = CompletableFuture<Chunk>()
        stubNativeBorder(world, platformBorder)
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(server.onlinePlayers).thenReturn(listOf(player))
        whenever(repository.get(world)).thenReturn(border)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.name).thenReturn("pending")
        whenever(player.world).thenReturn(world)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.location).thenReturn(Location(world, 15.0, 64.0, 0.0))
        whenever(world.getChunkAtAsyncUrgently(any<Location>())).thenReturn(pendingChunkLoad)
        val playerTeleporter = PlayerTeleporter(
            mock(SafeYLocationFinder::class.java),
            mock(TeleportHistoryStorage::class.java),
            server,
            Dispatchers.Unconfined,
        )
        val service = WorldBorderService(
            server,
            repository,
            broadcaster,
            playerTeleporter,
            Dispatchers.Unconfined,
        )

        val job = launch(start = CoroutineStart.UNDISPATCHED) { service.set(world, border) }
        job.cancelAndJoin()

        verify(repository).set(world, border)
        verify(broadcaster).broadcast(any<WorldBorderUpdatedEvent>())
    }

    private fun stubNativeBorder(
        world: World,
        border: BukkitWorldBorder,
        center: Location = Location(world, 0.0, 64.0, 0.0),
        size: Double = WorldBorderLimits.MAX_DIMENSION,
    ) {
        whenever(world.worldBorder).thenReturn(border)
        whenever(border.center).thenReturn(center)
        whenever(border.size).thenReturn(size)
    }
}
