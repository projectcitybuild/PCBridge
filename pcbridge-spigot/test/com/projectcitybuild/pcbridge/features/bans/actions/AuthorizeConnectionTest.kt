package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.http.models.PlayerData
import com.projectcitybuild.pcbridge.http.models.IPBan
import com.projectcitybuild.pcbridge.http.models.PlayerBan
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

class AuthorizeConnectionTest {
    private lateinit var useCase: AuthorizeConnection

    private fun playerBan() = PlayerBan(
        id = Random.nextInt(),
        bannedPlayerAlias = "alias",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    private fun ipBan() = IPBan(
        id = Random.nextInt(),
        ipAddress = "192.168.0.1",
        bannerPlayerId = Random.nextInt(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    @BeforeEach
    fun setUp() {
        useCase = AuthorizeConnection()
    }

    @Test
    fun `should deny if player is banned`() =
        runTest {
            val ban = playerBan()
            val result =
                useCase.authorize(
                    PlayerData(playerBan = ban),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Denied(
                    AuthorizeConnection.Ban.UUID(ban),
                ),
                result,
            )
        }

    @Test
    fun `should not deny if player ban is inactive`() =
        runTest {
            val ban =
                playerBan().copy(
                    unbannedAt = LocalDate
                        .now()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toLocalDateTime(),
                )
            val result =
                useCase.authorize(
                    PlayerData(playerBan = ban),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Allowed,
                result,
            )
        }

    @Test
    fun `should deny if IP is banned`() =
        runTest {
            val ban = ipBan()
            val result =
                useCase.authorize(
                    PlayerData(ipBan = ban),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Denied(
                    AuthorizeConnection.Ban.IP(ban),
                ),
                result,
            )
        }

    @Test
    fun `should not deny if IP ban is inactive`() =
        runTest {
            val ban =
                ipBan().copy(
                    unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime(),
                )
            val result =
                useCase.authorize(
                    PlayerData(ipBan = ban),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Allowed,
                result,
            )
        }

    @Test
    fun `should allow if not banned`() =
        runTest {
            val result =
                useCase.authorize(
                    PlayerData(playerBan = null, ipBan = null),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Allowed,
                result,
            )
        }
}
