package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.GameBanMock
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.stubs.IPBanMock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.net.SocketAddress
import java.util.UUID

class AuthoriseConnectionUseCaseTest {

    private lateinit var useCase: AuthoriseConnectionUseCase

    private lateinit var banRepository: BanRepository
    private lateinit var ipBanRepository: IPBanRepository

    @BeforeEach
    fun setUp() {
        banRepository = mock(BanRepository::class.java)
        ipBanRepository = mock(IPBanRepository::class.java)

        useCase = AuthoriseConnectionUseCase(
            banRepository,
            ipBanRepository,
        )
    }

    @Test
    fun `should return UUID ban if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val uuidBan = GameBanMock()
        val ip = "127.0.0.1"

        `when`(banRepository.get(uuid)).thenReturn(uuidBan)
        `when`(ipBanRepository.get(ip)).thenReturn(null)

        val ban = useCase.getBan(uuid, ip)

        assertEquals(ban, AuthoriseConnectionUseCase.Ban.UUID(uuidBan))
    }

    @Test
    fun `should return IP ban if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val ipBan = IPBanMock()

        `when`(banRepository.get(uuid)).thenReturn(null)
        `when`(ipBanRepository.get(ip)).thenReturn(ipBan)

        val ban = useCase.getBan(uuid, ip)

        assertEquals(ban, AuthoriseConnectionUseCase.Ban.IP(ipBan))
    }

    @Test
    fun `should return null if not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"

        `when`(banRepository.get(uuid)).thenReturn(null)
        `when`(ipBanRepository.get(ip)).thenReturn(null)

        val ban = useCase.getBan(uuid, ip)

        assertNull(ban)
    }
}
