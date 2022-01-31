package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.WarpMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.warp.WarpUseCase
import com.projectcitybuild.features.warps.usecases.warp.WarpUseCaseImpl
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.*

class WarpUseCaseImplTest {

    private lateinit var useCase: WarpUseCase

    private lateinit var plugin: Plugin
    private lateinit var warpRepository: WarpRepository
    private lateinit var queuedWarpRepository: QueuedWarpRepository
    private lateinit var nameGuesser: NameGuesser
    private lateinit var logger: PlatformLogger
    private lateinit var localEventBroadcaster: LocalEventBroadcaster
    private lateinit var player: Player
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        plugin = mock(Plugin::class.java)
        warpRepository = mock(WarpRepository::class.java)
        queuedWarpRepository = mock(QueuedWarpRepository::class.java)
        nameGuesser = mock(NameGuesser::class.java)
        logger = mock(PlatformLogger::class.java)
        localEventBroadcaster = mock(LocalEventBroadcaster::class.java)

        player = mock(Player::class.java)
        `when`(player.location).thenReturn(mock(Location::class.java))

        server = mock(Server::class.java)
        `when`(plugin.server).thenReturn(server)

        useCase = WarpUseCaseImpl(
            plugin,
            warpRepository,
            queuedWarpRepository,
            nameGuesser,
            logger,
            localEventBroadcaster,
        )
    }

    @Test
    fun `should fail if warp doesn't exist`() = runTest {
        `when`(warpRepository.all()).thenReturn(emptyList())

        val result = useCase.warp("warp", "server_name", player)

        assertEquals(result, Failure(WarpUseCase.FailureReason.WARP_NOT_FOUND))
    }

    @Test
    fun `should fail if world not found in same server warp`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)
        val server = mock(Server::class.java)

        `when`(warpRepository.all()).thenReturn(listOf(warp))
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(anyString())).thenReturn(null)

        val result = useCase.warp(warpName, warp.location.serverName, player)

        assertEquals(result, Failure(WarpUseCase.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should emit PlayerPreWarpEvent event if successful warp`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)
        val server = mock(Server::class.java)
        val world = mock(World::class.java)
        val location = Location(world, 1.0, 2.0, 3.0, 4f, 5f)

        `when`(player.location).thenReturn(location)
        `when`(warpRepository.all()).thenReturn(listOf(warp))
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(anyString())).thenReturn(world)

        val result = useCase.warp(warpName, warp.location.serverName, player)

        argumentCaptor<PlayerPreWarpEvent>().apply {
            verify(localEventBroadcaster, times(1)).emit(capture())

            assertEquals(firstValue.player, player)
            assertEquals(firstValue.currentLocation, location)
        }
        assertTrue(result is Success)
    }

    @Test
    fun `should teleport player if warp is in same server`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)
        val world = mock(World::class.java)

        `when`(warpRepository.all()).thenReturn(listOf(warp))
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(anyString())).thenReturn(world)

        val result = useCase.warp(warpName, warp.location.serverName, player)

        argumentCaptor<Location>().apply {
            verify(player, times(1)).teleport(capture())

            assertEquals(firstValue.world, world)
            assertEquals(firstValue.x, warp.location.x)
            assertEquals(firstValue.z, warp.location.z)
            assertEquals(firstValue.pitch, warp.location.pitch)
            assertEquals(firstValue.yaw, warp.location.yaw)
        }

        val expectedResult = Success(WarpUseCase.WarpEvent(warpName = warp.name, isSameServer = true))
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should queue warp request if warp is in different server`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)
        val world = mock(World::class.java)
        val playerUUID = UUID.randomUUID()

        `when`(player.uniqueId).thenReturn(playerUUID)
        `when`(warpRepository.all()).thenReturn(listOf(warp))
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(anyString())).thenReturn(world)

        val result = useCase.warp(warpName, "other_server", player)

        verify(queuedWarpRepository, times(1)).queue(playerUUID, warp)

        val expectedResult = Success(WarpUseCase.WarpEvent(warpName = warp.name, isSameServer = false))
        assertEquals(expectedResult, result)
    }

    @Test
    fun `should send player-server-switch request if warp is in different server`() = runTest {
        // TODO: do later when Server Messaging interface is injected
    }
}