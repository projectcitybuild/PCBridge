package com.projectcitybuild.pcbridge.paper.features.bans.actions

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.Stubs
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.libs.playerlookup.PlayerLookup
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CreateUuidBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.repositories.UuidBanRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class CreateUuidBanTest {
    private lateinit var createBan: CreateUuidBan
    private lateinit var playerLookup: PlayerLookup
    private lateinit var uuidBanRepository: UuidBanRepository
    private lateinit var manageUrlGenerator: ManageUrlGenerator

    @BeforeEach
    fun setUp() {
        playerLookup = mock(PlayerLookup::class.java)
        uuidBanRepository = mock(UuidBanRepository::class.java)
        manageUrlGenerator = mock(ManageUrlGenerator::class.java)

        createBan = CreateUuidBan(
            playerLookup = playerLookup,
            uuidBanRepository = uuidBanRepository,
            manageUrlGenerator = manageUrlGenerator,
        )
    }

    @Test
    fun `throws if player not found`() = runTest {
        whenever(playerLookup.findUuid(any()))
            .thenReturn(null)

        assertThrows<CreateUuidBan.PlayerNotFound> {
            createBan.create(
                bannedAlias = "banned_alias",
                bannerUuid = UUID.randomUUID(),
                bannerAlias = "banner_alias",
                reason = "reason",
                additionalInfo = "additional_info",
            )
        }
    }

    @Test
    fun `throws if player already banned`() = runTest {
        whenever(playerLookup.findUuid(any()))
            .thenReturn(UUID.randomUUID())
        whenever(
            uuidBanRepository.create(
                bannedUUID = any(),
                bannedAlias = any(),
                bannerUUID = any(),
                bannerAlias = any(),
                reason = any(),
                additionalInfo = any(),
            )
        ).thenThrow(ResponseParserError.Conflict(""))

        assertThrows<CreateUuidBan.PlayerAlreadyBanned> {
            createBan.create(
                bannedAlias = "banned_alias",
                bannerUuid = UUID.randomUUID(),
                bannerAlias = "banner_alias",
                reason = "reason",
                additionalInfo = "additional_info",
            )
        }
    }

    @Test
    fun `throws when input validation error`() = runTest {
        whenever(playerLookup.findUuid(any()))
            .thenReturn(UUID.randomUUID())
        whenever(
            uuidBanRepository.create(
                bannedUUID = any(),
                bannedAlias = any(),
                bannerUUID = any(),
                bannerAlias = any(),
                reason = any(),
                additionalInfo = any(),
            )
        ).thenThrow(ResponseParserError.Validation("bad input"))

        val error = assertThrows<CreateUuidBan.InvalidBanInput> {
            createBan.create(
                bannedAlias = "banned_alias",
                bannerUuid = UUID.randomUUID(),
                bannerAlias = "banner_alias",
                reason = "reason",
                additionalInfo = "additional_info",
            )
        }
        assertEquals("bad input", error.message)
    }

    @Test
    fun `creates ban successfully and returns creation object`() = runTest {
        val bannedUuid = UUID.randomUUID()

        whenever(playerLookup.findUuid(any()))
            .thenReturn(bannedUuid)

        val ban = Stubs.playerBan()

        whenever(
            uuidBanRepository.create(
                bannedUUID = any(),
                bannedAlias = any(),
                bannerUUID = any(),
                bannerAlias = any(),
                reason = any(),
                additionalInfo = any(),
            )
        ).thenReturn(ban)

        whenever(
            manageUrlGenerator.path("manage/player-bans/${ban.id}/edit")
        ).thenReturn("https://localhost/manage/player-bans/${ban.id}/edit")

        val result = createBan.create(
            bannedAlias = "banned_alias",
            bannerUuid = UUID.randomUUID(),
            bannerAlias = "banner_alias",
            reason = "reason",
            additionalInfo = "additional_info",
        )

        assertEquals(ban, result.ban)
        assertEquals(bannedUuid, result.bannedUuid)
        assertEquals(
            "https://localhost/manage/player-bans/${ban.id}/edit",
            result.editUrl
        )
    }
}
