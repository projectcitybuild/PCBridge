package com.projectcitybuild.pcbridge.core.observability.errors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ErrorTrackerTest {
    @Test
    fun `report should delegate throwable to Sentry provider`() {
        val provider = mock<SentryProvider>()
        val tracker = ErrorTracker(provider)
        val throwable = IllegalStateException("failure")

        tracker.report(throwable)

        verify(provider).report(throwable)
    }

    @Test
    fun `catching should report and rethrow failures`() {
        val provider = mock<SentryProvider>()
        val tracker = ErrorTracker(provider)
        val failure = IllegalStateException("failure")

        val thrown =
            assertThrows(IllegalStateException::class.java) {
                runBlocking {
                    tracker.catching<String> {
                        throw failure
                    }
                }
            }

        assertSame(failure, thrown)
        verify(provider).report(failure)
    }
}
