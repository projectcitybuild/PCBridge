package com.projectcitybuild.shared.locationteleport

import com.projectcitybuild.CrossServerLocationMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.shared.locationteleport.events.PlayerPreLocationTeleportEvent
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verifyNoInteractions
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.UUID

class LocationTeleporterTest {

    private lateinit var locationTeleporter: LocationTeleporter

    private lateinit var localEventBroadcaster: LocalEventBroadcaster
    private lateinit var server: Server
    private lateinit var logger: PlatformLogger

    private lateinit var player: Player

    companion object {
        private const val SERVER_NAME = "server"
    }

    @BeforeEach
    fun setUp() {
        localEventBroadcaster = mock(LocalEventBroadcaster::class.java)
        server = mock(Server::class.java)
        logger = mock(PlatformLogger::class.java)
        player = mock(Player::class.java).also {
            `when`(it.uniqueId).thenReturn(UUID.randomUUID())
            `when`(it.location).thenReturn(mock(Location::class.java))
        }

        locationTeleporter = LocationTeleporter(
            localEventBroadcaster,
            server,
            logger,
        )
    }

    @Test
    fun `should return failure if world not found on matching server`() = runTest {
        val destination = CrossServerLocationMock(serverName = SERVER_NAME)

        `when`(server.getWorld(destination.worldName)).thenReturn(null)

        val result = locationTeleporter.teleport(player, destination)

        assertEquals(result, Failure(LocationTeleporter.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should teleport if world found on matching server`() = runTest {
        val destination = CrossServerLocationMock(serverName = SERVER_NAME)
        val world = mock(World::class.java)

        `when`(server.getWorld(destination.worldName)).thenReturn(world)

        val result = locationTeleporter.teleport(player, destination)

        argumentCaptor<Location>().apply {
            verify(player).teleport(capture())

            assertEquals(firstValue.world, world)
            assertEquals(firstValue.x, destination.x)
            assertEquals(firstValue.y, destination.y)
            assertEquals(firstValue.z, destination.z)
            assertEquals(firstValue.pitch, destination.pitch)
            assertEquals(firstValue.yaw, destination.yaw)
        }
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should emit PlayerPreLocationTeleportEvent if successful teleport`() = runTest {
        val destination = CrossServerLocationMock(serverName = SERVER_NAME)

        // Assert no interaction because of failure
        `when`(server.getWorld(destination.worldName)).thenReturn(null)

        locationTeleporter.teleport(player, destination)

        argumentCaptor<PlayerPreLocationTeleportEvent>().apply {
            verifyNoInteractions(localEventBroadcaster)
        }

        // Assert interaction because of success
        val world = mock(World::class.java)
        `when`(server.getWorld(destination.worldName)).thenReturn(world)

        locationTeleporter.teleport(player, destination)

        argumentCaptor<PlayerPreLocationTeleportEvent>().apply {
            verify(localEventBroadcaster).emit(capture())

            assertEquals(firstValue.player, player)
            assertEquals(firstValue.currentLocation, player.location)
        }
    }
}
