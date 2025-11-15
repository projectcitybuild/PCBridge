package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.destinations

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ReportDestination
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import io.sentry.Sentry

class SentryReportDestination(
    private val dsn: String,
): ReportDestination {
    override fun start() {
        Sentry.init { options ->
            options.dsn = dsn
        }
        log.info { "Sentry error reporting enabled" }
    }

    override fun close() {
        Sentry.close()
        log.info { "Sentry error reporting disabled" }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
