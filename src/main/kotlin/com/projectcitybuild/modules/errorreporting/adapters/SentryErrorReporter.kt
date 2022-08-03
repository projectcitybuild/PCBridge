package com.projectcitybuild.modules.errorreporting.adapters

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import io.sentry.Sentry

@Reusable
class SentryErrorReporter(
    private val config: Config,
    private val logger: PlatformLogger,
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
