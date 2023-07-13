package com.projectcitybuild.libs.errorreporting.adapters

import com.projectcitybuild.libs.config.Config
import com.projectcitybuild.libs.config.ConfigKeys
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import io.sentry.Sentry

class SentryErrorReporter(
    private val config: Config,
    private val logger: PlatformLogger,
) : ErrorReporter {

    override fun start() {
        val isEnabled = config.get(ConfigKeys.errorReportingSentryEnabled)
        if (!isEnabled) return

        logger.info("Enabling error reporting")

        Sentry.init { options ->
            options.dsn = config.get(ConfigKeys.errorReportingSentryDSN)
        }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
