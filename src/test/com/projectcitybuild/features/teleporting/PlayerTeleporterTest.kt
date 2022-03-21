package com.projectcitybuild.features.teleporting

import com.projectcitybuild.stubs.PlayerConfigMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.modules.channels.NodeMessenger
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.repositories.QueuedPlayerTeleportRepository
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class PlayerTeleporterTest {

    private lateinit var requester: PlayerTeleporter

    private lateinit var playerConfigRepository: PlayerConfigRepository
    private lateinit var queuedPlayerTeleportRepository: QueuedPlayerTeleportRepository
    private lateinit var nodeMessenger: NodeMessenger

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)
        queuedPlayerTeleportRepository = mock(QueuedPlayerTeleportRepository::class.java)
        nodeMessenger = mock(NodeMessenger::class.java)

        requester = PlayerTeleporter(
            playerConfigRepository,
            queuedPlayerTeleportRepository,
            nodeMessenger,
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

        val result = requester.teleport(originPlayer, targetPlayer, true, false)

        assertEquals(result, Failure(PlayerTeleporter.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
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

        val result = requester.summon(targetPlayer, originPlayer, true, false)

        assertEquals(result, Failure(PlayerTeleporter.FailureReason.TARGET_PLAYER_DISALLOWS_TP))
    }
}
