package com.projectcitybuild.features.teleporting

import com.projectcitybuild.PlayerConfigMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.*

class PlayerTeleportRequesterTest {

    private lateinit var requester: PlayerTeleportRequester

    private val playerConfigRepository = mock(PlayerConfigRepository::class.java)
    private val queuedTeleportRepository = mock(QueuedTeleportRepository::class.java)

    @BeforeEach
    fun setUp() {
        requester = PlayerTeleportRequester(
            playerConfigRepository,
            queuedTeleportRepository,
        )
    }

    @Test
    fun `teleport should fail if the target player disallows teleports`() = runTest {
        val originPlayer = mock(ProxiedPlayer::class.java)

        val targetPlayerUUID = UUID.randomUUID()
        val targetPlayer = mock(ProxiedPlayer::class.java)
        `when`(targetPlayer.uniqueId).thenReturn(targetPlayerUUID)
        `when`(playerConfigRepository.get(targetPlayerUUID)).thenReturn(
            PlayerConfigMock(targetPlayerUUID).apply { isAllowingTPs = false }
        )

        val result = requester.teleport(originPlayer, targetPlayer, true)

        assertEquals(result, Failure(PlayerTeleportRequester.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
    }

    @Test
    fun `summon should fail if the target player disallows teleports`() = runTest {
        val originPlayer = mock(ProxiedPlayer::class.java)

        val targetPlayerUUID = UUID.randomUUID()
        val targetPlayer = mock(ProxiedPlayer::class.java)
        `when`(targetPlayer.uniqueId).thenReturn(targetPlayerUUID)
        `when`(playerConfigRepository.get(targetPlayerUUID)).thenReturn(
            PlayerConfigMock(targetPlayerUUID).apply { isAllowingTPs = false }
        )

        val result = requester.summon(targetPlayer, originPlayer, true)

        assertEquals(result, Failure(PlayerTeleportRequester.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
    }
}