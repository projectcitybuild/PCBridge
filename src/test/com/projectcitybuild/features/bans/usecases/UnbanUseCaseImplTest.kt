package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.UUID

class UnbanUseCaseImplTest {

    private lateinit var useCase: UnbanUseCase

    private lateinit var banRepository: BanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var messageBroadcaster: MessageBroadcaster

    @BeforeEach
    fun setUp() {
        banRepository = mock(BanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
        messageBroadcaster = mock(MessageBroadcaster::class.java)

        useCase = UnbanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            messageBroadcaster,
        )
    }

    @Test
    fun `unban should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(null)

        val result = useCase.unban(playerName, null)

        assertEquals(result, Failure(UnbanUseCase.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `unban should fail when player is not banned`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)
        `when`(banRepository.unban(playerUUID, staffUUID))
            .thenThrow(BanRepository.PlayerNotBannedException())

        val result = useCase.unban(playerName, staffUUID)

        assertEquals(result, Failure(UnbanUseCase.FailureReason.PlayerNotBanned))
    }

    @Test
    fun `unban should pass input to ban repository`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)

        useCase.unban(playerName, staffUUID)

        verify(banRepository, times(1))
            .unban(playerUUID, staffUUID)
    }

    @Test
    fun `unban should be broadcasted to all online players`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(UUID.randomUUID())

        useCase.unban(playerName, UUID.randomUUID())

        verify(messageBroadcaster, times(1))
            .broadcastToAll(any())
    }
}