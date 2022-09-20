package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.IPBanRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class UnbanIPTest {

    private lateinit var useCase: UnbanIP
    private lateinit var ipBanRepository: IPBanRepository

    @BeforeEach
    fun setUp() {
        ipBanRepository = mock(IPBanRepository::class.java)
        useCase = UnbanIP(ipBanRepository)
    }

    @Test
    fun `should fail if IP is not banned`() = runTest {
        val ip = "127.0.0.1"

        `when`(ipBanRepository.unban(eq(ip), any(), any()))
            .thenThrow(IPBanRepository.IPNotBannedException::class.java)

        val result = useCase.execute(
            ip = ip,
            unbannerUUID = UUID.randomUUID(),
            unbannerName = "name",
        )
        assertEquals(Failure(UnbanIP.FailureReason.IP_NOT_BANNED), result)
    }

    @Test
    fun `should fail if IP is invalid`() = runTest {
        arrayOf(
            "text",
            "1234",
        ).forEach { invalidIP ->
            val result = useCase.execute(
                ip = invalidIP,
                unbannerUUID = UUID.randomUUID(),
                unbannerName = "name",
            )
            assertEquals(Failure(UnbanIP.FailureReason.INVALID_IP), result)
        }
    }

    @Test
    fun `should unban valid IP`() = runTest {
        val uuid = UUID.randomUUID()
        val ips = arrayOf(
            "127.0.0.1",
            "/127.0.0.1:1234", // This should get sanitized
        )
        ips.forEach { ip ->
            val result = useCase.execute(
                ip = ip,
                unbannerUUID = uuid,
                unbannerName = "name",
            )
            verify(ipBanRepository).unban(
                ip = "127.0.0.1",
                unbannerUUID = uuid,
                unbannerName = "name",
            )
            assertEquals(Success(Unit), result)
        }
    }
}
