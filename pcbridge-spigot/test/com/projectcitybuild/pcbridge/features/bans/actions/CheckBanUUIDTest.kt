package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.core.datetime.DateTimeFormatter
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerBanRepository
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerUUIDRepository
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Success
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

class CheckBanUUIDTest {
    private lateinit var useCase: CheckUUIDBan

    private lateinit var playerBanRepository: PlayerBanRepository
    private lateinit var playerUUIDRepository: PlayerUUIDRepository
    private lateinit var dateTimeFormatter: DateTimeFormatter

    @BeforeEach
    fun setUp() {
        playerBanRepository = mock(PlayerBanRepository::class.java)
        playerUUIDRepository = mock(PlayerUUIDRepository::class.java)

        dateTimeFormatter =
            DateTimeFormatter(
                Locale.forLanguageTag("en-us"),
                ZoneId.of("UTC"),
            )

        useCase =
            CheckUUIDBan(
                playerBanRepository,
                playerUUIDRepository,
                dateTimeFormatter,
            )
    }

    @Test
    fun `getBan should fail when player doesn't exist`() =
        runTest {
            val playerName = "banned_player"

            whenever(playerUUIDRepository.get(playerName)).thenReturn(null)

            val result = useCase.getBan(playerName)

            assertEquals(result, Failure(CheckUUIDBan.FailureReason.PLAYER_DOES_NOT_EXIST))
        }

    @Test
    fun `getBan should return a ban record with no expiry date`() =
        runTest {
            val playerName = "banned_player"
            val playerUUID = UUID.randomUUID()
            val staffUUID = UUID.randomUUID()

            whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
            whenever(playerBanRepository.get(playerUUID)).thenReturn(
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
                ),
            )

            val result = useCase.getBan(playerName)
            val expected =
                CheckUUIDBan.BanRecord(
                    reason = "griefing",
                    dateOfBan = "Jan 23, 2022, 4:06:12 PM",
                    expiryDate = "Never",
                )

            assertEquals(Success(expected), result)
        }

    @Test
    fun `getBan should return a ban record with an expiry date`() =
        runTest {
            val playerName = "banned_player"
            val playerUUID = UUID.randomUUID()
            val staffUUID = UUID.randomUUID()

            whenever(playerUUIDRepository.get(playerName)).thenReturn(playerUUID)
            whenever(playerBanRepository.get(playerUUID)).thenReturn(
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
                ),
            )

            val result = useCase.getBan(playerName)
            val expected =
                CheckUUIDBan.BanRecord(
                    reason = "griefing",
                    dateOfBan = "Jan 23, 2022, 4:06:12 PM",
                    expiryDate = "Jan 25, 2022, 11:06:05 AM",
                )

            assertEquals(Success(expected), result)
        }
}
