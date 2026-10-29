package com.projectcitybuild.pcbridge.runtime.connection.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionMiddlewareChain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.InetAddress
import java.util.UUID

class AuthorizeConnectionListenerTest {
    @Test
    fun `disallows login and propagates authorization cancellation without reporting it`() = runBlocking {
        val cancellation = CancellationException("authorization cancelled")
        val connectionRepository = mock<ConnectionRepository>()
        whenever(connectionRepository.auth(any(), anyOrNull())).thenThrow(cancellation)
        val sentry = mock<SentryProvider>()
        val listener = AuthorizeConnectionListener(
            mock<ConnectionMiddlewareChain>(),
            connectionRepository,
            mock<SpigotEventBroadcaster>(),
            ErrorTracker(sentry),
        )
        val event = mock<AsyncPlayerPreLoginEvent>()
        whenever(event.uniqueId).thenReturn(UUID.randomUUID())
        whenever(event.address).thenReturn(InetAddress.getLoopbackAddress())

        val thrown = assertThrows<CancellationException> {
            listener.handle(event)
        }

        assertSame(cancellation, thrown)
        verify(event).disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
            l10n.connection.errorUnableToAuthConnection,
        )
        verify(sentry, never()).report(cancellation)
    }
}
