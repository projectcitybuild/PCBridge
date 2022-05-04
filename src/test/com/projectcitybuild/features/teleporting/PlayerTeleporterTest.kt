package com.projectcitybuild.features.teleporting

import com.projectcitybuild.PlayerConfigMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.repositories.PlayerConfigRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class PlayerTeleporterTest {

    private lateinit var requester: PlayerTeleporter
    private lateinit var playerConfigRepository: PlayerConfigRepository

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)

        requester = PlayerTeleporter(playerConfigRepository)
    }

    @Test
    fun `teleport should fail if the target player disallows teleports`() = runTest {
        val originPlayer = mock(Player::class.java)
        val targetPlayer = mock(Player::class.java)
        val targetPlayerUUID = UUID.randomUUID()

        `when`(targetPlayer.uniqueId).thenReturn(targetPlayerUUID)
        `when`(playerConfigRepository.get(targetPlayerUUID)).thenReturn(
            PlayerConfigMock(targetPlayerUUID).apply { isAllowingTPs = false }
        )

        val result = requester.teleport(originPlayer, targetPlayer, shouldCheckAllowingTP = true, shouldSupressTeleportedMessage = false)

        assertEquals(result, Failure(PlayerTeleporter.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
    }

    @Test
    fun `summon should fail if the target player disallows teleports`() = runTest {
        val originPlayer = mock(Player::class.java)
        val targetPlayer = mock(Player::class.java)
        val targetPlayerUUID = UUID.randomUUID()

        `when`(targetPlayer.uniqueId).thenReturn(targetPlayerUUID)
        `when`(playerConfigRepository.get(targetPlayerUUID)).thenReturn(
            PlayerConfigMock(targetPlayerUUID).apply { isAllowingTPs = false }
        )

        val result = requester.summon(targetPlayer, originPlayer, shouldCheckAllowingTP = true, shouldSupressTeleportedMessage = false)

        assertEquals(result, Failure(PlayerTeleporter.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
    }
}
