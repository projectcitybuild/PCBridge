package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.WarpMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.LastKnownLocationRepository
import com.projectcitybuild.repositories.WarpRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`

class TeleportToWarpUseCaseTest {

    private lateinit var useCase: TeleportToWarpUseCase

    private lateinit var warpRepository: WarpRepository
    private lateinit var nameGuesser: NameGuesser
    private lateinit var server: Server
    private lateinit var player: Player

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)
        nameGuesser = mock(NameGuesser::class.java)
        server = mock(Server::class.java)

        player = mock(Player::class.java).also {
            `when`(it.location).thenReturn(mock(Location::class.java))
        }

        useCase = TeleportToWarpUseCase(
            warpRepository = warpRepository,
            nameGuesser = nameGuesser,
            logger = mock(PlatformLogger::class.java),
            lastKnownLocationRepository = mock(LastKnownLocationRepository::class.java),
            server = server,
        )
    }

    @Test
    fun `should fail if warp doesn't exist`() = runTest {
        `when`(warpRepository.names()).thenReturn(emptyList())

        val result = useCase.warp(player, "warp")

        assertEquals(result, Failure(TeleportToWarpUseCase.FailureReason.WARP_NOT_FOUND))
    }

    @Test
    fun `should fail if world not found`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)

        `when`(warpRepository.names()).thenReturn(listOf(warp.name))
        `when`(warpRepository.first(warpName)).thenReturn(warp)
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(anyString())).thenReturn(null)

        val result = useCase.warp(player, warpName)

        assertEquals(result, Failure(TeleportToWarpUseCase.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should teleport player if possible`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)
        val world = mock(World::class.java)

        `when`(warpRepository.names()).thenReturn(listOf(warp.name))
        `when`(warpRepository.first(warpName)).thenReturn(warp)
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(server.getWorld(warp.location.worldName)).thenReturn(world)

        val result = useCase.warp(player, warpName)

        verify(player).teleport(
            Location(
                world,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                warp.location.yaw,
                warp.location.pitch,
            )
        )

        assertEquals(
            Success(TeleportToWarpUseCase.WarpEvent(warpName = warp.name)),
            result,
        )
    }
}
