package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.destinations

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ReportDestination
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import io.sentry.Sentry

class SentryReportDestination(
    private val dsn: String,
    private val environment: String,
): ReportDestination {
    override fun start() {
        Sentry.init { options ->
            options.dsn = dsn
            options.environment = environment
        }
        deprecatedLog.info { "Sentry error reporting enabled" }
    }

    override fun close() {
        Sentry.close()
        deprecatedLog.info { "Sentry error reporting disabled" }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
