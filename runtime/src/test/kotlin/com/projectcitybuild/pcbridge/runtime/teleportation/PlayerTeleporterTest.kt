package com.projectcitybuild.pcbridge.runtime.teleportation

import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportDestinationRejectedException
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlayerTeleporterTest {
    @Test
    fun `loads the destination chunk before resolving safe y and teleporting`() = runBlocking {
        val fixture = fixture()
        val sequence = mutableListOf<String>()
        whenever(fixture.world.getChunkAtAsyncUrgently(fixture.destination)).thenAnswer {
            sequence += "chunk"
            CompletableFuture.completedFuture(mock<Chunk>())
        }
        whenever(fixture.world.isChunkLoaded(1, -1)).thenReturn(true)
        whenever(fixture.safeYLocationFinder.findY(fixture.world, 16, -1)).thenAnswer {
            sequence += "safe-y"
            70
        }
        whenever(fixture.player.teleportAsync(any<Location>(), eq(TeleportCause.PLUGIN))).thenAnswer {
            sequence += "teleport"
            CompletableFuture.completedFuture(true)
        }

        fixture.teleporter.move(
            fixture.player,
            fixture.destination,
            options = PlayerTeleporter.TeleportOptions(
                adjustYForSafety = true,
                playSound = false,
                recordHistory = false,
            ),
        )

        assertEquals(listOf("chunk", "safe-y", "teleport"), sequence)
        val destination = argumentCaptor<Location>()
        verify(fixture.player).teleportAsync(destination.capture(), eq(TeleportCause.PLUGIN))
        assertEquals(70.0, destination.firstValue.y)
    }

    @Test
    fun `rejects a destination that changes before teleport starts`() = runBlocking {
        val fixture = fixture()
        whenever(fixture.world.getChunkAtAsyncUrgently(fixture.destination))
            .thenReturn(CompletableFuture.completedFuture(mock<Chunk>()))
        whenever(fixture.world.isChunkLoaded(1, -1)).thenReturn(true)
        whenever(fixture.safeYLocationFinder.findY(fixture.world, 16, -1)).thenReturn(70)

        assertThrows(TeleportDestinationRejectedException::class.java) {
            runBlocking {
                fixture.teleporter.moveValidated(
                    player = fixture.player,
                    destination = fixture.destination,
                    options = PlayerTeleporter.TeleportOptions(adjustYForSafety = true),
                    validateDestination = { false },
                )
            }
        }

        verify(fixture.player, never()).teleportAsync(any<Location>(), any<TeleportCause>())
    }

    @Test
    fun `does not teleport when the loaded destination has no safe landing`() = runBlocking {
        val fixture = fixture()
        whenever(fixture.world.getChunkAtAsyncUrgently(fixture.destination))
            .thenReturn(CompletableFuture.completedFuture(mock<Chunk>()))
        whenever(fixture.world.isChunkLoaded(1, -1)).thenReturn(true)
        whenever(fixture.safeYLocationFinder.findY(fixture.world, 16, -1)).thenReturn(null)

        assertThrows(SafeDestinationNotFoundException::class.java) {
            runBlocking {
                fixture.teleporter.move(
                    fixture.player,
                    fixture.destination,
                    options = PlayerTeleporter.TeleportOptions(adjustYForSafety = true),
                )
            }
        }

        verify(fixture.player, never()).teleportAsync(any<Location>(), any<TeleportCause>())
    }

    @Test
    fun `maintains destination y when safety adjustment is disabled`() = runBlocking {
        val fixture = fixture()
        whenever(fixture.player.teleportAsync(any<Location>(), eq(TeleportCause.PLUGIN)))
            .thenReturn(CompletableFuture.completedFuture(true))

        fixture.teleporter.move(
            fixture.player,
            fixture.destination,
            options = PlayerTeleporter.TeleportOptions(
                preloadDestinationChunk = false,
                adjustYForSafety = false,
                playSound = false,
                recordHistory = false,
            ),
        )

        val destination = argumentCaptor<Location>()
        verify(fixture.player).teleportAsync(destination.capture(), eq(TeleportCause.PLUGIN))
        assertEquals(fixture.destination.y, destination.firstValue.y)
        verify(fixture.safeYLocationFinder, never()).findY(any(), any(), any())
    }

    @Test
    fun `plays teleport sound at the actual safe destination`() = runBlocking {
        val fixture = fixture()
        val finalLocation = Location(fixture.world, 16.5, 70.0, -0.5)
        whenever(fixture.player.location)
            .thenReturn(Location(fixture.world, 0.0, 64.0, 0.0), finalLocation)
        whenever(fixture.world.getChunkAtAsyncUrgently(fixture.destination))
            .thenReturn(CompletableFuture.completedFuture(mock<Chunk>()))
        whenever(fixture.world.isChunkLoaded(1, -1)).thenReturn(true)
        whenever(fixture.safeYLocationFinder.findY(fixture.world, 16, -1)).thenReturn(70)
        whenever(fixture.player.teleportAsync(any<Location>(), eq(TeleportCause.PLUGIN)))
            .thenReturn(CompletableFuture.completedFuture(true))

        fixture.teleporter.move(
            fixture.player,
            fixture.destination,
            options = PlayerTeleporter.TeleportOptions(
                adjustYForSafety = true,
                snapToBlockCenter = true,
                recordHistory = false,
            ),
        )

        verify(fixture.world).playSound(
            finalLocation,
            "minecraft:entity.player.teleport",
            1F,
            1F,
        )
    }

    @Test
    fun `propagates cancellation from the asynchronous teleport operation`() {
        val fixture = fixture()
        whenever(fixture.player.teleportAsync(any<Location>(), any<TeleportCause>()))
            .thenReturn(CompletableFuture<Boolean>().also { it.cancel(false) })

        assertThrows(kotlinx.coroutines.CancellationException::class.java) {
            runBlocking {
                fixture.teleporter.move(
                    fixture.player,
                    fixture.destination,
                    options = PlayerTeleporter.TeleportOptions(
                        preloadDestinationChunk = false,
                        playSound = false,
                    ),
                )
            }
        }
    }

    private fun fixture(): Fixture {
        val world = mock<World>()
        val server = mock<Server>()
        val player = mock<Player>()
        val worldId = UUID.randomUUID()
        val destination = Location(world, 16.25, 0.0, -0.75)
        whenever(world.uid).thenReturn(worldId)
        whenever(server.getWorld(worldId)).thenReturn(world)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.location).thenReturn(Location(world, 0.0, 64.0, 0.0))
        val safeYLocationFinder = mock<SafeYLocationFinder>()
        val teleporter = PlayerTeleporter(
            safeYLocationFinder,
            mock<TeleportHistoryStorage>(),
            server,
            Dispatchers.Unconfined,
        )
        return Fixture(world, player, destination, safeYLocationFinder, teleporter)
    }

    private data class Fixture(
        val world: World,
        val player: Player,
        val destination: Location,
        val safeYLocationFinder: SafeYLocationFinder,
        val teleporter: PlayerTeleporter,
    )
}
