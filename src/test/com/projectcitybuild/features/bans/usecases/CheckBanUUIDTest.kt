package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.PlayerBan
import com.projectcitybuild.repositories.PlayerBanRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class CheckBanUUIDTest {

    private lateinit var useCase: CheckUUIDBan

    private lateinit var playerBanRepository: PlayerBanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository

    @BeforeEach
    fun setUp() {
        playerBanRepository = mock(PlayerBanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)

        useCase = CheckUUIDBan(
            playerBanRepository,
            playerUUIDRepository,
            DateTimeFormatterMock()
        )
    }

    @Test
    fun `getBan should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.get(playerName)).thenReturn(null)

        val result = useCase.getBan(playerName)

        assertEquals(result, Failure(CheckUUIDBan.FailureReason.PLAYER_DOES_NOT_EXIST))
    }

    @Test
    fun `getBan should return a ban record with no expiry date`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
        `when`(playerBanRepository.get(playerUUID)).thenReturn(
            PlayerBan(
                id = 1,
                serverId = 2,
                bannedPlayerId = playerUUID.toString(),
                bannedPlayerAlias = playerName,
                bannerPlayerId = staffUUID.toString(),
                reason = "griefing",
                expiresAt = null,
                createdAt = 1642953972,
                updatedAt = 1642953972,
                unbannedAt = null,
                unbannerPlayerId = null,
                unbanType = null,
            )
        )

        val result = useCase.getBan(playerName)
        val expected = CheckUUIDBan.BanRecord(
            reason = "griefing",
            dateOfBan = "Jan 23, 2022, 4:06:12 PM",
            expiryDate = "Never"
        )

        assertEquals(Success(expected), result)
    }

    @Test
    fun `getBan should return a ban record with an expiry date`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
        `when`(playerBanRepository.get(playerUUID)).thenReturn(
            PlayerBan(
                id = 1,
                serverId = 2,
                bannedPlayerId = playerUUID.toString(),
                bannedPlayerAlias = playerName,
                bannerPlayerId = staffUUID.toString(),
                reason = "griefing",
                expiresAt = 1643108765,
                createdAt = 1642953972,
                updatedAt = 1642953972,
                unbannedAt = null,
                unbannerPlayerId = null,
                unbanType = null,
            )
        )

        val result = useCase.getBan(playerName)
        val expected = CheckUUIDBan.BanRecord(
            reason = "griefing",
            dateOfBan = "Jan 23, 2022, 4:06:12 PM",
            expiryDate = "Jan 25, 2022, 11:06:05 AM"
        )

        assertEquals(Success(expected), result)
    }
}
