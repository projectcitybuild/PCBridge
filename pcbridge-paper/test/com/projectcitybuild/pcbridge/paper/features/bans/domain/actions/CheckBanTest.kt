package com.projectcitybuild.pcbridge.paper.features.bans.domain.actions

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.paper.Stubs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class CheckBanTest {
    private lateinit var useCase: CheckBan

    @BeforeEach
    fun setUp() {
        useCase = CheckBan()
    }

    @Test
    fun `should deny if player is banned`() =
        runTest {
            val ban = Stubs.playerBan()
            val result = useCase.check(
                PlayerData(playerBan = ban),
            )
            assertEquals(CheckBan.Ban.UUID(ban), result)
        }

    @Test
    fun `should not deny if player ban is inactive`() =
        runTest {
            val ban = Stubs.playerBan().copy(
                unbannedAt = LocalDate
                    .now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toLocalDateTime(),
            )
            val result = useCase.check(
                PlayerData(playerBan = ban),
            )
            assertNull(result)
        }

    @Test
    fun `should deny if IP is banned`() =
        runTest {
            val ban = Stubs.ipBan()
            val result = useCase.check(
                PlayerData(ipBan = ban),
            )
            assertEquals(CheckBan.Ban.IP(ban), result)
        }

    @Test
    fun `should not deny if IP ban is inactive`() =
        runTest {
            val ban = Stubs.ipBan().copy(
                unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime(),
            )
            val result = useCase.check(
                PlayerData(ipBan = ban),
            )
            assertNull(result)
        }

    @Test
    fun `should allow if not banned`() =
        runTest {
            val result = useCase.check(
                PlayerData(playerBan = null, ipBan = null),
            )
            assertNull(result)
        }
}
