package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import io.sentry.Sentry
import io.sentry.SentryOpenTelemetryMode

class SentryProvider(
    private val dsn: String,
    private val environment: String,
    private val traceSampleRate: Double?,
) {
    fun init() {
        if (dsn.isEmpty()) {
            logSync.warn { "Sentry DSN not specified. Error reporting will not be available" }
            return
        }
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = environment
            options.logs.isEnabled = true
            options.tracesSampleRate = traceSampleRate
            options.openTelemetryMode = SentryOpenTelemetryMode.AGENTLESS
        }
        logSync.info { "Sentry error reporting enabled" }
    }

    fun close() {
        if (dsn.isEmpty()) return

        Sentry.close()
        logSync.info { "Sentry error reporting disabled" }
    }

    fun report(throwable: Throwable) {
        if (! Sentry.isEnabled()) {
            logSync.warn { "Sentry not enabled. Error will be discarded" }
            return
        }
        Sentry.captureException(throwable)
    }
}