package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.stubs.WarpMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.integrations.internal.crossteleport.LocationTeleporter
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.WarpRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`

class WarpUseCaseTest {

    private lateinit var useCase: WarpUseCase

    private lateinit var warpRepository: WarpRepository
    private lateinit var locationTeleporter: LocationTeleporter
    private lateinit var nameGuesser: NameGuesser
    private lateinit var player: Player

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)
        locationTeleporter = mock(LocationTeleporter::class.java)
        nameGuesser = mock(NameGuesser::class.java)

        player = mock(Player::class.java).also {
            `when`(it.location).thenReturn(mock(Location::class.java))
        }

        useCase = WarpUseCase(
            warpRepository,
            nameGuesser,
            locationTeleporter,
        )
    }

    @Test
    fun `should fail if warp doesn't exist`() = runTest {
        `when`(warpRepository.names()).thenReturn(emptyList())

        val result = useCase.warp(player, "warp")

        assertEquals(result, Failure(WarpUseCase.FailureReason.WARP_NOT_FOUND))
    }

    @Test
    fun `should fail if world not found in target server`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)

        `when`(warpRepository.names()).thenReturn(listOf(warp.name))
        `when`(warpRepository.first(warpName)).thenReturn(warp)
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(locationTeleporter.teleport(player, warp.location, warp.name)).thenReturn(
            Failure(LocationTeleporter.FailureReason.WORLD_NOT_FOUND)
        )

        val result = useCase.warp(player, warpName)

        assertEquals(result, Failure(WarpUseCase.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should teleport player if possible`() = runTest {
        val warpName = "warp"
        val warp = WarpMock(warpName)

        `when`(warpRepository.names()).thenReturn(listOf(warp.name))
        `when`(warpRepository.first(warpName)).thenReturn(warp)
        `when`(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        `when`(locationTeleporter.teleport(player, warp.location, warp.name)).thenReturn(
            Success(LocationTeleporter.DestinationType.CROSS_SERVER)
        )
        assertEquals(
            useCase.warp(player, warpName),
            Success(WarpUseCase.WarpEvent(warpName = warp.name, isSameServer = false))
        )

        `when`(locationTeleporter.teleport(player, warp.location, warp.name)).thenReturn(
            Success(LocationTeleporter.DestinationType.SAME_SERVER)
        )
        assertEquals(
            useCase.warp(player, warpName),
            Success(WarpUseCase.WarpEvent(warpName = warp.name, isSameServer = true))
        )
    }
}
