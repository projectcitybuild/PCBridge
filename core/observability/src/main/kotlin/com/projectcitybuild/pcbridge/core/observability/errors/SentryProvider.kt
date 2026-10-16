package com.projectcitybuild.pcbridge.core.observability.errors

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import io.sentry.Sentry
import io.sentry.SentryOpenTelemetryMode

class SentryClient {
    fun init(
        dsn: String,
        environment: String,
        traceSampleRate: Double?,
    ) {
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = environment
            options.logs.isEnabled = true
            options.tracesSampleRate = traceSampleRate
            options.openTelemetryMode = SentryOpenTelemetryMode.AGENTLESS
        }
    }

    fun isEnabled() = Sentry.isEnabled()

    fun close() {
        Sentry.close()
    }

    fun captureException(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}

class SentryProvider(
    private val dsn: String,
    private val environment: String,
    private val traceSampleRate: Double?,
    private val sentryClient: SentryClient = SentryClient(),
) {
    fun init() {
        if (dsn.isEmpty()) {
            logSync.warn { "Sentry DSN not specified. Error reporting will not be available" }
            return
        }
        sentryClient.init(dsn, environment, traceSampleRate)
        logSync.info { "Sentry error reporting enabled" }
    }

    fun close() {
        if (dsn.isEmpty()) return

        sentryClient.close()
        logSync.info { "Sentry error reporting disabled" }
    }

    fun report(throwable: Throwable) {
        if (!sentryClient.isEnabled()) {
            logSync.warn { "Sentry not enabled. Error will be discarded" }
            return
        }
        sentryClient.captureException(throwable)
    }
}
