package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.GameBanMock
import com.projectcitybuild.features.bans.usecases.AuthoriseConnectionUseCase
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.plugin.listeners.BanConnectionListener
import com.projectcitybuild.stubs.IPBanMock
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLoginEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.net.InetSocketAddress
import java.util.UUID

class BanConnectionListenerTest {

    private lateinit var listener: BanConnectionListener

    private lateinit var authoriseConnectionListener: AuthoriseConnectionUseCase
    private lateinit var errorReporter: ErrorReporter

    @BeforeEach
    fun setUp() {
        authoriseConnectionListener = mock(AuthoriseConnectionUseCase::class.java)
        errorReporter = mock(ErrorReporter::class.java)

        listener = BanConnectionListener(
            authoriseConnectionListener,
            mock(PlatformLogger::class.java),
            DateTimeFormatterMock(),
            errorReporter,
        )
    }

    private fun loginEvent(uuid: UUID, ip: String = "127.0.0.1"): PlayerLoginEvent {
        val socketAddress = mock(InetSocketAddress::class.java).also {
            `when`(it.toString()).thenReturn(ip)
        }
        val player = mock(Player::class.java).also {
            `when`(it.address).thenReturn(socketAddress)
            `when`(it.uniqueId).thenReturn(uuid)
        }
        return mock(PlayerLoginEvent::class.java).also {
            `when`(it.player).thenReturn(player)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        arrayOf(
            AuthoriseConnectionUseCase.Ban.UUID(GameBanMock()),
            AuthoriseConnectionUseCase.Ban.IP(IPBanMock()),
        ).forEach { ban ->
            val uuid = UUID.randomUUID()
            val ip = "127.0.0.1"
            val event = loginEvent(uuid, ip)

            `when`(authoriseConnectionListener.getBan(uuid, ip)).thenReturn(ban)

            listener.onLoginEvent(event)

            verify(event).disallow(eq(PlayerLoginEvent.Result.KICK_BANNED), anyString())
        }
    }

    @Test
    fun `does not cancel login event if player is not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenReturn(null)

        listener.onLoginEvent(event)

        verify(event, never()).disallow(any(), any())
    }

    @Test
    fun `cancels login event if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenThrow(Exception())

        listener.onLoginEvent(event)

        verify(event).disallow(eq(PlayerLoginEvent.Result.KICK_OTHER), anyString())
    }

    @Test
    fun `reports error if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)
        val exception = Exception()

        `when`(authoriseConnectionListener.getBan(uuid, ip)).thenThrow(exception)

        listener.onLoginEvent(event)

        verify(errorReporter).report(exception)
    }
}
