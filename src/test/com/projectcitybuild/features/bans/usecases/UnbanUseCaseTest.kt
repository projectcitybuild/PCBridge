package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.UUID

class UnbanUseCaseTest {

    private lateinit var useCase: UnbanUseCase

    private lateinit var banRepository: BanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        banRepository = mock(BanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
        server = mock(Server::class.java)

        useCase = UnbanUseCase(
            banRepository,
            playerUUIDRepository,
            server,
        )
    }

    @Test
    fun `unban should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.get(playerName)).thenReturn(null)

        val result = useCase.unban(playerName, null)

        assertEquals(result, Failure(UnbanUseCase.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `unban should fail when player is not banned`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
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

        `when`(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)

        useCase.unban(playerName, staffUUID)

        verify(banRepository, times(1))
            .unban(playerUUID, staffUUID)
    }

    @Test
    fun `unban should be broadcasted to all online players`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.get(playerName)).thenReturn(UUID.randomUUID())

        useCase.unban(playerName, UUID.randomUUID())

        verify(server).broadcastMessage(any())
    }
}
