package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.GameBanMock
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.stubs.IPBanMock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Plugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.net.SocketAddress
import java.util.*

class BanConnectionListenerTest {

    private lateinit var listener: BanConnectionListener

    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var banRepository: BanRepository
    private lateinit var ipBanRepository: IPBanRepository

    @BeforeEach
    fun setUp() {
        banRepository = mock(BanRepository::class.java)
        ipBanRepository = mock(IPBanRepository::class.java)
        scheduler = TestCoroutineScheduler()

        listener = BanConnectionListener(
            mock(Plugin::class.java),
            banRepository,
            ipBanRepository,
            mock(PlatformLogger::class.java),
            DateTimeFormatterMock(),
            mock(ErrorReporter::class.java),
        ).apply {
            dispatcher = StandardTestDispatcher(scheduler)
        }
    }

    private fun mockedEvent(ip: String = "127.0.0.1", uuid: UUID = UUID.randomUUID()): LoginEvent {
        val socketAddress = mock(SocketAddress::class.java).also {
            `when`(it.toString()).thenReturn(ip)
        }
        val connection = mock(PendingConnection::class.java).also {
            `when`(it.socketAddress).thenReturn(socketAddress)
            `when`(it.uniqueId).thenReturn(uuid)
        }
        return mock(LoginEvent::class.java).also {
            `when`(it.connection).thenReturn(connection)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        val playerIP = "127.0.0.1"
        val playerUUID = UUID.randomUUID()
        val event = mockedEvent(playerIP)
        val connection = mock(PendingConnection::class.java)

        `when`(banRepository.get(playerUUID)).thenReturn(GameBanMock())
        `when`(ipBanRepository.get(playerIP)).thenReturn(null)
        `when`(event.connection).thenReturn(connection)
        `when`(connection.uniqueId).thenReturn(playerUUID)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event).isCancelled = true
        verify(event).setCancelReason(any(BaseComponent::class.java))
    }

    @Test
    fun `cancels login event if IP is banned`() = runTest {
        val playerIP = "127.0.0.1"
        val playerUUID = UUID.randomUUID()
        val event = mockedEvent(playerIP, playerUUID)

        `when`(banRepository.get(playerUUID)).thenReturn(null)
        `when`(ipBanRepository.get(playerIP)).thenReturn(IPBanMock(playerIP))

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event).isCancelled = true
        verify(event).setCancelReason(any(BaseComponent::class.java))
    }

    @Test
    fun `does not cancel login event if player and IP is not banned`() = runTest {
        val playerIP = "127.0.0.1"
        val playerUUID = UUID.randomUUID()
        val event = mockedEvent(playerIP, playerUUID)

        `when`(banRepository.get(playerUUID)).thenReturn(null)
        `when`(ipBanRepository.get(playerIP)).thenReturn(null)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event, never()).isCancelled
        verify(event, never()).setCancelReason(any(BaseComponent::class.java))
    }
}