package com.projectcitybuild.modules.errorreporting.adapters

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.support.spigot.logger.Logger
import io.sentry.Sentry

class SentryErrorReporter(
    private val config: Config,
    private val logger: Logger,
) : ErrorReporter {

    override fun bootstrap() {
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
