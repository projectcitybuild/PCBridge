package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.PlayerBanMock
import com.projectcitybuild.features.aggregate.ConnectPlayer
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.stubs.IPBanMock
import com.projectcitybuild.support.spigot.logger.Logger
import kotlinx.coroutines.test.runTest
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.net.InetAddress
import java.util.UUID

class AsyncPreLoginListenerTest {

    private lateinit var listener: AsyncPreLoginListener

    private lateinit var connectPlayer: ConnectPlayer
    private lateinit var errorReporter: ErrorReporter

    @BeforeEach
    fun setUp() {
        connectPlayer = mock(ConnectPlayer::class.java)
        errorReporter = mock(ErrorReporter::class.java)

        listener = AsyncPreLoginListener(
            connectPlayer = connectPlayer,
            mock(Logger::class.java),
            DateTimeFormatterMock(),
            errorReporter,
        )
    }

    private fun loginEvent(uuid: UUID, ip: String = "127.0.0.1"): AsyncPlayerPreLoginEvent {
        val socketAddress = mock(InetAddress::class.java).also {
            `when`(it.toString()).thenReturn(ip)
        }
        return mock(AsyncPlayerPreLoginEvent::class.java).also {
            `when`(it.address).thenReturn(socketAddress)
            `when`(it.uniqueId).thenReturn(uuid)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        arrayOf(
            ConnectPlayer.Ban.UUID(PlayerBanMock()),
            ConnectPlayer.Ban.IP(IPBanMock()),
        ).forEach { ban ->
            val uuid = UUID.randomUUID()
            val ip = "127.0.0.1"
            val event = loginEvent(uuid, ip)

            `when`(connectPlayer.execute(uuid, ip))
                .thenReturn(ConnectPlayer.ConnectResult.Denied(ban))

            listener.onAsyncPreLogin(event)

            verify(event).disallow(eq(AsyncPlayerPreLoginEvent.Result.KICK_BANNED), anyString())
        }
    }

    @Test
    fun `does not cancel login event if player is not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        `when`(connectPlayer.execute(uuid, ip))
            .thenReturn(null)

        listener.onAsyncPreLogin(event)

        verify(event, never()).disallow(any(AsyncPlayerPreLoginEvent.Result::class.java), any())
    }

    @Test
    fun `cancels login event if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        `when`(connectPlayer.execute(uuid, ip))
            .thenThrow(Exception())

        listener.onAsyncPreLogin(event)

        verify(event).disallow(eq(AsyncPlayerPreLoginEvent.Result.KICK_OTHER), anyString())
    }

    @Test
    fun `reports error if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)
        val exception = Exception()

        `when`(connectPlayer.execute(uuid, ip))
            .thenThrow(exception)

        listener.onAsyncPreLogin(event)

        verify(errorReporter).report(exception)
    }
}
