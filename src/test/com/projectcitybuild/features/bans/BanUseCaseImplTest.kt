package com.projectcitybuild.test.com.projectcitybuild.features.bans

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.usecases.BanUseCase
import com.projectcitybuild.features.bans.usecases.BanUseCaseImpl
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.UUID

class BanUseCaseImplTest {

    private lateinit var useCase: BanUseCaseImpl

    private val banRepository = mock(BanRepository::class.java)
    private val playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
    private val playerKicker = mock(PlayerKicker::class.java)
    private val messageBroadcaster = mock(MessageBroadcaster::class.java)

    @BeforeEach
    fun setUp() {
        useCase = BanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            playerKicker,
            messageBroadcaster,
        )
    }

    @Test
    fun `ban should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(null)

        val result = useCase.ban(playerName, null, "staff_player", null)

        assertEquals(result, Failure(BanUseCase.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `ban should fail when player is already banned`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffName = "staff_player"
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)
        `when`(banRepository.ban(playerUUID, playerName, staffUUID, null))
            .thenThrow(BanRepository.PlayerAlreadyBannedException())

        val result = useCase.ban(playerName, staffUUID, staffName, null)

        assertEquals(result, Failure(BanUseCase.FailureReason.PlayerAlreadyBanned))
    }

    @Test
    fun `ban should pass input to ban repository`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()
        val reason = "reason"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)

        useCase.ban(playerName, staffUUID, "staff_player", reason)

        verify(banRepository, times(1))
            .ban(playerUUID, playerName, staffUUID, reason)
    }

    @Test
    fun `ban should be broadcasted to all online players`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(UUID.randomUUID())

        useCase.ban(playerName, UUID.randomUUID(), "staff_player", "reason")

        verify(messageBroadcaster, times(1))
            .broadcastToAll(any())
    }

    @Test
    fun `ban should kick the online player`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)

        useCase.ban(playerName, UUID.randomUUID(), "staff_player", "reason")

        verify(playerKicker, times(1))
            .kick(eq(playerUUID), anyString(), eq(PlayerKicker.KickContext.FATAL))
    }
}