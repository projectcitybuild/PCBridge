package com.projectcitybuild.modules.errorreporting.adapters

import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import io.sentry.Sentry

@Reusable
class SentryErrorReporter(
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
): ErrorReporter {

    override fun bootstrap() {
        val enabled = config.get(ConfigKeys.ERROR_REPORTING_SENTRY_ENABLED)
        if (!enabled) return

        logger.info("Enabling error reporting")

        Sentry.init { options ->
            options.dsn = config.get(ConfigKeys.ERROR_REPORTING_SENTRY_DSN)
        }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}