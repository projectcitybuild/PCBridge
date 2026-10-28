package com.projectcitybuild.pcbridge.runtime.exceptions.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import kotlinx.coroutines.CancellationException
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CoroutineExceptionListenerTest {
    @Test
    fun `uncaught cancellation is not reported`() {
        val plugin = mock<JavaPlugin>()
        val event = mock<MCCoroutineExceptionEvent>()
        val sentry = mock<SentryProvider>()
        val cancellation = CancellationException("cancelled")
        whenever(event.plugin).thenReturn(plugin)
        whenever(event.exception).thenReturn(cancellation)

        CoroutineExceptionListener(plugin, ErrorTracker(sentry)).onException(event)

        verify(sentry, never()).report(cancellation)
    }
}
