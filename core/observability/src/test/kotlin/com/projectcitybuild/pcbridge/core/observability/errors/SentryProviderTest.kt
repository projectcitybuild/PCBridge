package com.projectcitybuild.pcbridge.core.observability.errors

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SentryProviderTest {
    @Test
    fun `empty DSN should skip initialization and close and discard reports`() {
        val sentryClient = mock<SentryClient>()
        whenever(sentryClient.isEnabled()).thenReturn(false)
        val provider = SentryProvider(
            dsn = "",
            environment = "test",
            traceSampleRate = null,
            sentryClient = sentryClient,
        )

        provider.init()
        provider.report(IllegalStateException("ignored while Sentry is disabled"))
        provider.close()

        verify(sentryClient).isEnabled()
        verifyNoMoreInteractions(sentryClient)
    }

    @Test
    fun `init should pass configuration to Sentry and close it`() {
        val sentryClient = mock<SentryClient>()
        val dsn = "https://public@example.test/42"
        val environment = "test-environment"
        val traceSampleRate = 0.25
        val provider = SentryProvider(
            dsn = dsn,
            environment = environment,
            traceSampleRate = traceSampleRate,
            sentryClient = sentryClient,
        )

        provider.init()
        provider.close()

        verify(sentryClient).init(dsn, environment, traceSampleRate)
        verify(sentryClient).close()
        verifyNoMoreInteractions(sentryClient)
    }

    @Test
    fun `report should capture throwable when Sentry is enabled`() {
        val sentryClient = mock<SentryClient>()
        whenever(sentryClient.isEnabled()).thenReturn(true)
        val provider =
            SentryProvider(
                dsn = "https://public@example.test/42",
                environment = "test",
                traceSampleRate = null,
                sentryClient = sentryClient,
            )
        val failure = IllegalStateException("failure")

        provider.report(failure)

        verify(sentryClient).isEnabled()
        verify(sentryClient).captureException(failure)
        verify(sentryClient, never()).close()
        verifyNoMoreInteractions(sentryClient)
    }
}
