package com.projectcitybuild.modules.pluginutils.listeners

import com.projectcitybuild.DateTimeFormatterMock
import com.projectcitybuild.features.aggregate.AuthoriseConnection
import com.projectcitybuild.features.aggregate.GetAggregate
import com.projectcitybuild.features.aggregate.SyncPlayerWithAggregate
import com.projectcitybuild.modules.moderation.bans.listeners.AsyncPreLoginListener
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import kotlinx.coroutines.test.runTest
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.net.InetAddress
import java.util.UUID

class AsyncPreLoginListenerTest {
    private lateinit var getAggregate: GetAggregate
    private lateinit var authoriseConnection: AuthoriseConnection
    private lateinit var syncPlayerWithAggregate: SyncPlayerWithAggregate
    private lateinit var errorReporter: ErrorReporter
    private lateinit var listener: AsyncPreLoginListener

    @BeforeEach
    fun setUp() {
        getAggregate = mock(GetAggregate::class.java)
        authoriseConnection = mock(AuthoriseConnection::class.java)
        syncPlayerWithAggregate = mock(SyncPlayerWithAggregate::class.java)
        errorReporter = mock(ErrorReporter::class.java)

        listener = AsyncPreLoginListener(
            getAggregate,
            authoriseConnection,
            syncPlayerWithAggregate,
            mock(PlatformLogger::class.java),
            DateTimeFormatterMock(),
            errorReporter,
        )
    }

    private fun loginEvent(uuid: UUID, ip: String = "127.0.0.1"): AsyncPlayerPreLoginEvent {
        val socketAddress = mock(InetAddress::class.java).also {
            whenever(it.toString()).thenReturn(ip)
        }
        return mock(AsyncPlayerPreLoginEvent::class.java).also {
            whenever(it.address).thenReturn(socketAddress)
            whenever(it.uniqueId).thenReturn(uuid)
        }
    }

    @Test
    fun `cancels login event if player is banned`() = runTest {
        arrayOf(
            AuthoriseConnection.Ban.UUID(PlayerBan()),
            AuthoriseConnection.Ban.IP(IPBan()),
        ).forEach { ban ->
            val uuid = UUID.randomUUID()
            val ip = "127.0.0.1"
            val event = loginEvent(uuid, ip)

            whenever(getAggregate.execute(uuid, ip))
                .thenReturn(Aggregate())

            whenever(authoriseConnection.execute(Aggregate()))
                .thenReturn(AuthoriseConnection.ConnectResult.Denied(ban))

            listener.handle(event)

            verify(event).disallow(eq(AsyncPlayerPreLoginEvent.Result.KICK_BANNED), anyString())
        }
    }

    @Test
    fun `does not cancel login event if player is not banned`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        whenever(getAggregate.execute(uuid, ip))
            .thenReturn(Aggregate())

        whenever(authoriseConnection.execute(Aggregate()))
            .thenReturn(null)

        listener.handle(event)

        verify(event, never()).disallow(any(AsyncPlayerPreLoginEvent.Result::class.java), any())
    }

    @Test
    fun `cancels login event if cannot fetch ban`() = runTest {
        val uuid = UUID.randomUUID()
        val ip = "127.0.0.1"
        val event = loginEvent(uuid, ip)

        whenever(getAggregate.execute(uuid, ip))
            .thenThrow(Exception::class.java)

        listener.handle(event)

        verify(event).disallow(eq(AsyncPlayerPreLoginEvent.Result.KICK_OTHER), anyString())
    }
}
