package com.projectcitybuild.pcbridge.runtime.listeners

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.core.observability.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.bukkit.event.Event
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ListenerExceptionHandlerTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun configureLogger() {
            Logger.configure("pcbridge.listener-exception-test")
        }
    }

    @Test
    fun `suspending listener boundary reports and consumes selected failures`() =
        runBlocking {
            val sentry = mock<SentryProvider>()
            val event = mock<Event>()
            val failure = IllegalStateException("listener failed")
            val errorTracker = ErrorTracker(sentry)
            var recovered = false

            event.runListenerSafely(
                errorTracker,
                "Listener failed",
                onFailure = { recovered = true },
            ) {
                throw failure
            }

            assertTrue(recovered)
            verify(sentry).report(failure)
        }

    @Test
    fun `suspending listener boundary propagates cancellation without reporting`() =
        runBlocking {
            val sentry = mock<SentryProvider>()
            val event = mock<Event>()
            val cancellation = CancellationException("cancelled")
            val errorTracker = ErrorTracker(sentry)
            var recovered = false

            val thrown =
                assertThrows<CancellationException> {
                    runBlocking {
                        event.runListenerSafely(
                            errorTracker,
                            "Listener failed",
                            onFailure = { recovered = true },
                        ) {
                            throw cancellation
                        }
                    }
                }

            assertSame(cancellation, thrown)
            assertTrue(!recovered)
            verify(sentry, never()).report(cancellation)
        }

    @Test
    fun `synchronous listener boundary reports and consumes selected failures`() {
        val sentry = mock<SentryProvider>()
        val event = mock<Event>()
        val failure = IllegalStateException("listener failed")
        val errorTracker = ErrorTracker(sentry)
        var recovered = false

        event.runListenerSafelySync(
            errorTracker,
            "Listener failed",
            onFailure = { recovered = true },
        ) {
            throw failure
        }

        assertTrue(recovered)
        verify(sentry).report(failure)
    }

    @Test
    fun `synchronous listener boundary propagates cancellation without reporting`() {
        val sentry = mock<SentryProvider>()
        val event = mock<Event>()
        val cancellation = CancellationException("cancelled")
        val errorTracker = ErrorTracker(sentry)

        val thrown =
            assertThrows<CancellationException> {
                event.runListenerSafelySync(errorTracker, "Listener failed") {
                    throw cancellation
                }
            }

        assertSame(cancellation, thrown)
        verify(sentry, never()).report(cancellation)
    }
}
