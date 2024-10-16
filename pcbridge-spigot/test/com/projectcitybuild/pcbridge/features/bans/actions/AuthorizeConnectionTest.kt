package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.http.responses.PlayerData
import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class AuthorizeConnectionTest {
    private lateinit var useCase: AuthorizeConnection

    @BeforeEach
    fun setUp() {
        useCase = AuthorizeConnection()
    }

    @Test
    fun `should deny if player is banned`() =
        runTest {
            val ban = PlayerBan()
            val result =
                useCase.execute(
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
                PlayerBan(
                    unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
                )
            val result =
                useCase.execute(
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
            val ban = IPBan()
            val result =
                useCase.execute(
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
                IPBan(
                    unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
                )
            val result =
                useCase.execute(
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
                useCase.execute(
                    PlayerData(playerBan = null, ipBan = null),
                )
            assertEquals(
                AuthorizeConnection.ConnectResult.Allowed,
                result,
            )
        }
}
