package com.projectcitybuild.pcbridge.core.observability.errors

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    fun `catching should report failures as Result failure`() =
        runBlocking {
            val provider = mock<SentryProvider>()
            val tracker = ErrorTracker(provider)
            val failure = IllegalStateException("failure")

            val result = tracker.catching<String> {
                throw failure
            }

            assertSame(failure, result.exceptionOrNull())
            verify(provider).report(failure)
        }

    @Test
    fun `catching preserves original failure when reporting fails`() =
        runBlocking {
            val provider = mock<SentryProvider>()
            val tracker = ErrorTracker(provider)
            val failure = IllegalStateException("failure")
            val reportingFailure = IllegalArgumentException("reporting failed")
            doThrow(reportingFailure).whenever(provider).report(failure)

            val result = tracker.catching<String> { throw failure }

            assertSame(failure, result.exceptionOrNull())
            assertSame(reportingFailure, failure.suppressed.single())
        }

    @Test
    fun `catching propagates cancellation without reporting`() =
        runBlocking {
            val provider = mock<SentryProvider>()
            val tracker = ErrorTracker(provider)
            val cancellation = CancellationException("cancelled")

            val thrown = runCatching {
                tracker.catching<String> { throw cancellation }
            }.exceptionOrNull()

            assertSame(cancellation, thrown)
            assertTrue(thrown is CancellationException)
            verify(provider, never()).report(cancellation)
        }
}
