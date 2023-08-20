package com.projectcitybuild.modules.moderation.bans.actions

import com.projectcitybuild.modules.moderation.bans.actions.AuthoriseConnection
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class AuthoriseConnectionTest {

    private lateinit var useCase: AuthoriseConnection

    @BeforeEach
    fun setUp() {
        useCase = AuthoriseConnection()
    }

    @Test
    fun `should deny if player is banned`() = runTest {
        val ban = PlayerBan()
        val result = useCase.execute(
            Aggregate(playerBan = ban)
        )
        assertEquals(
            AuthoriseConnection.ConnectResult.Denied(
                AuthoriseConnection.Ban.UUID(ban)
            ),
            result,
        )
    }

    @Test
    fun `should not deny if player ban is inactive`() = runTest {
        val ban = PlayerBan(
            unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        )
        val result = useCase.execute(
            Aggregate(playerBan = ban)
        )
        assertEquals(
            AuthoriseConnection.ConnectResult.Allowed,
            result,
        )
    }

    @Test
    fun `should deny if IP is banned`() = runTest {
        val ban = IPBan()
        val result = useCase.execute(
            Aggregate(ipBan = ban)
        )
        assertEquals(
            AuthoriseConnection.ConnectResult.Denied(
                AuthoriseConnection.Ban.IP(ban)
            ),
            result,
        )
    }

    @Test
    fun `should not deny if IP ban is inactive`() = runTest {
        val ban = IPBan(
            unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        )
        val result = useCase.execute(
            Aggregate(ipBan = ban)
        )
        assertEquals(
            AuthoriseConnection.ConnectResult.Allowed,
            result,
        )
    }

    @Test
    fun `should allow if not banned`() = runTest {
        val result = useCase.execute(
            Aggregate(playerBan = null, ipBan = null)
        )
        assertEquals(
            AuthoriseConnection.ConnectResult.Allowed,
            result,
        )
    }
}
