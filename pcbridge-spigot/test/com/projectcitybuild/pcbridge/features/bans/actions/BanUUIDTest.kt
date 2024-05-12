package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerUUIDRepository
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import com.projectcitybuild.pcbridge.utils.Failure
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class BanUUIDTest {
    private lateinit var useCase: BanUUID

    private lateinit var playerBanRepository: PlayerBanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var server: Server

    @BeforeEach
    fun setUp() {
        playerBanRepository = mock(PlayerBanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
        server = mock(Server::class.java)

        useCase =
            BanUUID(
                playerBanRepository,
                playerUUIDRepository,
                server,
            )
    }

    @Test
    fun `ban should fail when player doesn't exist`() =
        runTest {
            val playerName = "banned_player"

            whenever(playerUUIDRepository.get(playerName)).thenReturn(null)

            val result = useCase.ban(playerName, null, "staff_player", null)

            assertEquals(result, Failure(BanUUID.FailureReason.PlayerDoesNotExist))
        }

    @Test
    fun `ban should fail when player is already banned`() =
        runTest {
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
    fun `ban should pass input to ban repository`() =
        runTest {
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
}
