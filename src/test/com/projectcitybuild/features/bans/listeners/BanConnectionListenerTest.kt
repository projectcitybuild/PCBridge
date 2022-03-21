package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.GameBanMock
import com.projectcitybuild.features.bans.usecases.AuthoriseConnectionUseCase
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
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.net.SocketAddress
import java.util.UUID

class BanConnectionListenerTest {

    private lateinit var listener: BanConnectionListener

    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var authoriseConnectionListener: AuthoriseConnectionUseCase
    private lateinit var errorReporter: ErrorReporter

    @BeforeEach
    fun setUp() {
        authoriseConnectionListener = mock(AuthoriseConnectionUseCase::class.java)
        scheduler = TestCoroutineScheduler()
        errorReporter = mock(ErrorReporter::class.java)

        listener = BanConnectionListener(
            mock(Plugin::class.java),
            authoriseConnectionListener,
            mock(PlatformLogger::class.java),
            DateTimeFormatterMock(),
            errorReporter,
        ).apply {
            dispatcher = StandardTestDispatcher(scheduler)
        }
    }

    private fun loginEvent(uuid: UUID, ip: SocketAddress): LoginEvent {
        val connection = mock(PendingConnection::class.java).also {
            `when`(it.socketAddress).thenReturn(ip)
            `when`(it.uniqueId).thenReturn(uuid)
        }
        return mock(LoginEvent::class.java).also {
            `when`(it.connection).thenReturn(connection)
        }
    }

    private fun socketAddress(ip: String = "127.0.0.1"): SocketAddress {
        return mock(SocketAddress::class.java).also {
            `when`(it.toString()).thenReturn(ip)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        arrayOf(
            AuthoriseConnectionUseCase.Ban.UUID(GameBanMock()),
            AuthoriseConnectionUseCase.Ban.IP(IPBanMock()),
        ).forEach { ban ->
            val uuid = UUID.randomUUID()
            val ip = socketAddress()
            val event = loginEvent(uuid, ip)

            `when`(authoriseConnectionListener.getBan(uuid, ip)).thenReturn(ban)

            listener.onLoginEvent(event)
            scheduler.runCurrent()

            verify(event).isCancelled = true
            verify(event).setCancelReason(any(BaseComponent::class.java))
            verify(event).completeIntent(any())
        }
    }

    @Test
    fun `does not cancel login event if player is not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = socketAddress()
        val event = loginEvent(uuid, ip)

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenReturn(null)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event, never()).isCancelled
        verify(event, never()).setCancelReason(any(BaseComponent::class.java))
        verify(event).completeIntent(any())
    }

    @Test
    fun `cancels login event if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = socketAddress()
        val event = loginEvent(uuid, ip)

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenThrow(Exception())

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(event).isCancelled = true
        verify(event).setCancelReason(any(BaseComponent::class.java))
        verify(event).completeIntent(any())
    }

    @Test
    fun `reports error if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = socketAddress()
        val event = loginEvent(uuid, ip)
        val exception = Exception()

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenThrow(exception)

        listener.onLoginEvent(event)
        scheduler.runCurrent()

        verify(errorReporter).report(exception)
    }
}
