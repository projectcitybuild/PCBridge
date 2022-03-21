package com.projectcitybuild.features.bans.usecases

import com.projectcitybuild.stubs.GameBanMock
import com.projectcitybuild.repositories.BanRepository
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.stubs.IPBanMock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
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

    private fun socketAddress(ip: String = "127.0.0.1"): SocketAddress {
        return mock(SocketAddress::class.java).also {
            `when`(it.toString()).thenReturn(ip)
        }
    }

    @Test
    fun `should return UUID ban if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val uuidBan = GameBanMock()
        val ip = socketAddress()

        `when`(banRepository.get(uuid)).thenReturn(uuidBan)
        `when`(ipBanRepository.get(ip.toString())).thenReturn(null)

        val ban = useCase.getBan(uuid, socketAddress())

        assertEquals(ban, AuthoriseConnectionUseCase.Ban.UUID(uuidBan))
    }

    @Test
    fun `should return IP ban if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = socketAddress()
        val ipBan = IPBanMock()

        `when`(banRepository.get(uuid)).thenReturn(null)
        `when`(ipBanRepository.get(ip.toString())).thenReturn(ipBan)

        val ban = useCase.getBan(uuid, socketAddress())

        assertEquals(ban, AuthoriseConnectionUseCase.Ban.IP(ipBan))
    }

    @Test
    fun `should return null if not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = socketAddress()

        `when`(banRepository.get(uuid)).thenReturn(null)
        `when`(ipBanRepository.get(ip.toString())).thenReturn(null)

        val ban = useCase.getBan(uuid, socketAddress())

        assertNull(ban)
    }
}
