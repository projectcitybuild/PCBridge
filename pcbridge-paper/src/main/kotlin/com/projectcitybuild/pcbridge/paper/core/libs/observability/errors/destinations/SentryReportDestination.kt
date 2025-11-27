package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.destinations

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ReportDestination
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import io.sentry.Sentry

class SentryReportDestination(
    private val dsn: String,
    private val environment: String,
): ReportDestination {
    override fun start() {
        if (dsn.isEmpty()) {
            logSync.warn { "Sentry DSN not specified. Error reporting will not be available" }
            return
        }
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = environment
            options.logs.isEnabled = true
        }
        logSync.info { "Sentry error reporting enabled" }
    }

    override fun close() {
        if (dsn.isEmpty()) return

        Sentry.close()
        logSync.info { "Sentry error reporting disabled" }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
