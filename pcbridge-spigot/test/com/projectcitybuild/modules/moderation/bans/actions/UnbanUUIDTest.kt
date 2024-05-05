package com.projectcitybuild.modules.moderation.bans.actions

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.features.bans.repositories.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

class UnbanUUIDTest {

    private lateinit var useCase: UnbanUUID

    private lateinit var playerBanRepository: PlayerBanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        playerBanRepository = mock(PlayerBanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
        server = mock(Server::class.java)

        useCase = UnbanUUID(
            playerBanRepository,
            playerUUIDRepository,
            server,
        )
    }

    @Test
    fun `unban should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        whenever(playerUUIDRepository.get(playerName)).thenReturn(null)

        val result = useCase.unban(playerName, null)

        assertEquals(result, Failure(UnbanUUID.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `unban should fail when player is not banned`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
        whenever(playerBanRepository.unban(playerUUID, staffUUID))
            .thenThrow(UUIDBanHttpService.UUIDNotBannedException())

        val result = useCase.unban(playerName, staffUUID)

        assertEquals(result, Failure(UnbanUUID.FailureReason.PlayerNotBanned))
    }

    @Test
    fun `unban should pass input to ban repository`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)

        useCase.unban(playerName, staffUUID)

        verify(playerBanRepository, times(1))
            .unban(playerUUID, staffUUID)
    }

    @Test
    fun `unban should be broadcasted to all online players`() = runTest {
        val playerName = "banned_player"

        whenever(playerUUIDRepository.get(playerName)).thenReturn(UUID.randomUUID())

        useCase.unban(playerName, UUID.randomUUID())

        verify(server).broadcastMessage(any())
    }
}
