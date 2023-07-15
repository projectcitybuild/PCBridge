package com.projectcitybuild.modules.moderation.bans.actions

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import com.projectcitybuild.support.spigot.SpigotServer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.UUID

class BanUUIDTest {

    private lateinit var useCase: BanUUID

    private lateinit var playerBanRepository: PlayerBanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var server: SpigotServer

    @BeforeEach
    fun setUp() {
        playerBanRepository = mock(PlayerBanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
        server = mock(SpigotServer::class.java)

        useCase = BanUUID(
            playerBanRepository,
            playerUUIDRepository,
            server,
        )
    }

    @Test
    fun `ban should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        whenever(playerUUIDRepository.get(playerName)).thenReturn(null)

        val result = useCase.ban(playerName, null, "staff_player", null)

        assertEquals(result, Failure(BanUUID.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `ban should fail when player is already banned`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffName = "staff_player"
        val staffUUID = UUID.randomUUID()

        whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
        whenever(playerBanRepository.ban(playerUUID, playerName, staffUUID, staffName, null))
            .thenThrow(UUIDBanHttpService.UUIDAlreadyBannedException())

        val result = useCase.ban(playerName, staffUUID, staffName, null)

        assertEquals(result, Failure(BanUUID.FailureReason.PlayerAlreadyBanned))
    }

    @Test
    fun `ban should pass input to ban repository`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()
        val staffName = "staff_player"
        val reason = "reason"

        whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)

        useCase.ban(playerName, staffUUID, "staff_player", reason)

        verify(playerBanRepository, times(1))
            .ban(playerUUID, playerName, staffUUID, staffName, reason)
    }

    @Test
    fun `ban should be broadcasted to all online players`() = runTest {
        val playerName = "banned_player"

        whenever(playerUUIDRepository.get(playerName)).thenReturn(UUID.randomUUID())

        useCase.ban(playerName, UUID.randomUUID(), "staff_player", "reason")

        verify(server).broadcastMessage(any())
    }

    @Test
    fun `ban should kick the online player`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()

        whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)

        useCase.ban(playerName, UUID.randomUUID(), "staff_player", "reason")

        verify(server, times(1))
            .kickByUUID(eq(playerUUID), anyString(), eq(SpigotServer.KickContext.FATAL))
    }
}
