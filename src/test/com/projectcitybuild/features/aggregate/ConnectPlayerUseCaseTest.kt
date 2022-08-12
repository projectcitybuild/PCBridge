package com.projectcitybuild.features.aggregate

import com.projectcitybuild.GameBanMock
import com.projectcitybuild.entities.responses.Aggregate
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.adapters.MemoryKeyValueStorage
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.repositories.AggregateRepository
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.repositories.IPBanRepository
import com.projectcitybuild.stubs.IPBanMock
import com.projectcitybuild.support.spigot.logger.Logger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class ConnectPlayerUseCaseTest {

    private lateinit var useCase: ConnectPlayerUseCase

    private lateinit var permissions: Permissions
    private lateinit var aggregateRepository: AggregateRepository
    private lateinit var ipBanRepository: IPBanRepository
    private lateinit var chatBadgeRepository: ChatBadgeRepository
    private lateinit var config: Config

    @BeforeEach
    fun setUp() {
        permissions = mock(Permissions::class.java)
        aggregateRepository = mock(AggregateRepository::class.java)
        ipBanRepository = mock(IPBanRepository::class.java)
        chatBadgeRepository = mock(ChatBadgeRepository::class.java)
        config = Config(keyValueStorage = MemoryKeyValueStorage())

        useCase = ConnectPlayerUseCase(
            permissions,
            aggregateRepository,
            ipBanRepository,
            chatBadgeRepository,
            config,
            mock(Logger::class.java),
        )
    }

    @Test
    fun `returns denied (UUID ban) if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val uuidBan = GameBanMock()
        val ip = "127.0.0.1"
        val aggregate = Aggregate(
            account = null,
            ban = uuidBan,
            badges = emptyList(),
            donationPerks = emptyList(),
        )

        `when`(aggregateRepository.get(uuid)).thenReturn(aggregate)
        `when`(ipBanRepository.get(ip)).thenReturn(null)

        val ban = useCase.execute(uuid, ip)

        assertEquals(ban, ConnectPlayerUseCase.ConnectResult.Denied(
            ConnectPlayerUseCase.Ban.UUID(uuidBan))
        )
    }

    @Test
    fun `returns denied (IP ban) if banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val ipBan = IPBanMock()
        val aggregate = Aggregate(
            account = null,
            ban = null,
            badges = emptyList(),
            donationPerks = emptyList(),
        )

        `when`(aggregateRepository.get(uuid)).thenReturn(aggregate)
        `when`(ipBanRepository.get(ip)).thenReturn(ipBan)

        val ban = useCase.execute(uuid, ip)

        assertEquals(ban, ConnectPlayerUseCase.ConnectResult.Denied(
            ConnectPlayerUseCase.Ban.IP(ipBan))
        )
    }

    @Test
    fun `returns allowed if player not banned`() = runTest {

    }

    @Test
    fun `returns failed if exception thrown`() = runTest {

    }

    @Test
    fun `syncs player groups and donation tiers`() = runTest {

    }

    @Test
    fun `caches player badges`() = runTest {

    }
}
