package com.projectcitybuild.modules.warps.actions

import com.projectcitybuild.entities.Warp
import com.projectcitybuild.libs.nameguesser.NameGuesser
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TeleportToWarpTest {

    private lateinit var useCase: TeleportToWarp

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
            whenever(it.location).thenReturn(mock(Location::class.java))
        }

        useCase = TeleportToWarp(
            warpRepository = warpRepository,
            nameGuesser = nameGuesser,
            logger = mock(PlatformLogger::class.java),
            localEventBroadcaster = mock(LocalEventBroadcaster::class.java),
            server = server,
        )
    }

    @Test
    fun `should fail if warp doesn't exist`() = runTest {
        whenever(warpRepository.names()).thenReturn(emptyList())

        val result = useCase.warp(player, "warp")

        assertEquals(result, Failure(TeleportToWarp.FailureReason.WARP_NOT_FOUND))
    }

    @Test
    fun `should fail if world not found`() = runTest {
        val warpName = "warp"
        val warp = Warp(name = warpName)

        whenever(warpRepository.names()).thenReturn(listOf(warp.name))
        whenever(warpRepository.first(warpName)).thenReturn(warp)
        whenever(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        whenever(server.getWorld(anyString())).thenReturn(null)

        val result = useCase.warp(player, warpName)

        assertEquals(result, Failure(TeleportToWarp.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should teleport player if possible`() = runTest {
        val warpName = "warp"
        val warp = Warp(name = warpName)
        val world = mock(World::class.java)

        whenever(warpRepository.names()).thenReturn(listOf(warp.name))
        whenever(warpRepository.first(warpName)).thenReturn(warp)
        whenever(nameGuesser.guessClosest(any(), any())).thenReturn(warpName)
        whenever(server.getWorld(warp.location.worldName)).thenReturn(world)

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
            Success(TeleportToWarp.Warp(warpName = warp.name)),
            result,
        )
    }
}
