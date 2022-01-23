package com.projectcitybuild.features.bans

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.GameBan
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.usecases.CheckBanUseCase
import com.projectcitybuild.features.bans.usecases.CheckBanUseCaseImpl
import com.projectcitybuild.modules.datetime.DateTimeFormatter
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.time.ZoneId
import java.util.*

class CheckBanUseCaseImplTest {

    private lateinit var useCase: CheckBanUseCase

    private val banRepository = mock(BanRepository::class.java)
    private val playerUUIDRepository = mock(PlayerUUIDRepository::class.java)

    @BeforeEach
    fun setUp() {
        useCase = CheckBanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            dateTimeFormatter = DateTimeFormatter(
                Locale.forLanguageTag("en-us"),
                ZoneId.of("UTC")
            )
        )
    }

    @Test
    fun `getBan should fail when player doesn't exist`() = runTest {
        val playerName = "banned_player"

        `when`(playerUUIDRepository.request(playerName)).thenReturn(null)

        val result = useCase.getBan(playerName)

        assertEquals(result, Failure(CheckBanUseCase.FailureReason.PlayerDoesNotExist))
    }

    @Test
    fun `getBan should return a ban record with no expiry date`() = runTest {
        val playerName = "banned_player"
        val playerUUID = UUID.randomUUID()
        val staffUUID = UUID.randomUUID()

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)
        `when`(banRepository.get(playerUUID)).thenReturn(
            GameBan(
                id = 1,
                serverId = 2,
                playerId = playerUUID.toString(),
                playerType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffUUID.toString(),
                staffType = "minecraft_uuid",
                reason = "griefing",
                isActive = true,
                isGlobalBan = true,
                expiresAt = null,
                createdAt = 1642953972,
                updatedAt = 1642953972,
            )
        )

        val result = useCase.getBan(playerName)
        val expected = CheckBanUseCase.BanRecord(
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

        `when`(playerUUIDRepository.request(playerName)).thenReturn(playerUUID)
        `when`(banRepository.get(playerUUID)).thenReturn(
            GameBan(
                id = 1,
                serverId = 2,
                playerId = playerUUID.toString(),
                playerType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffUUID.toString(),
                staffType = "minecraft_uuid",
                reason = "griefing",
                isActive = true,
                isGlobalBan = true,
                expiresAt = 1643108765,
                createdAt = 1642953972,
                updatedAt = 1642953972,
            )
        )

        val result = useCase.getBan(playerName)
        val expected = CheckBanUseCase.BanRecord(
            reason = "griefing",
            dateOfBan = "Jan 23, 2022, 4:06:12 PM",
            expiryDate = "Jan 25, 2022, 11:06:05 AM"
        )

        assertEquals(Success(expected), result)
    }
}