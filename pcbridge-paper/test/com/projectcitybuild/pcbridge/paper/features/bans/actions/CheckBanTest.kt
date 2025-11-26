package com.projectcitybuild.pcbridge.paper.features.bans.actions

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CheckBan
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

class CheckBanTest {
    private lateinit var useCase: CheckBan

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
        useCase = CheckBan()
    }

    @Test
    fun `should deny if player is banned`() =
        runTest {
            val ban = playerBan()
            val result = useCase.get(
                PlayerData(playerBan = ban),
            )
            assertEquals(CheckBan.Ban.UUID(ban), result)
        }

    @Test
    fun `should not deny if player ban is inactive`() =
        runTest {
            val ban = playerBan().copy(
                unbannedAt = LocalDate
                    .now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toLocalDateTime(),
            )
            val result = useCase.get(
                PlayerData(playerBan = ban),
            )
            assertNull(result)
        }

    @Test
    fun `should deny if IP is banned`() =
        runTest {
            val ban = ipBan()
            val result = useCase.get(
                PlayerData(ipBan = ban),
            )
            assertEquals(CheckBan.Ban.IP(ban), result)
        }

    @Test
    fun `should not deny if IP ban is inactive`() =
        runTest {
            val ban = ipBan().copy(
                unbannedAt = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime(),
            )
            val result = useCase.get(
                PlayerData(ipBan = ban),
            )
            assertNull(result)
        }

    @Test
    fun `should allow if not banned`() =
        runTest {
            val result = useCase.get(
                PlayerData(playerBan = null, ipBan = null),
            )
            assertNull(result)
        }
}
