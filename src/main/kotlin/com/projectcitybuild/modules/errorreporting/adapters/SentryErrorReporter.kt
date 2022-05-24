package com.projectcitybuild.modules.errorreporting.adapters

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import io.sentry.Sentry

@Reusable
class SentryErrorReporter(
    private val config: ConfigKeys,
    private val logger: PlatformLogger,
) : ErrorReporter {

    override fun bootstrap() {
        val enabled = config.get(ConfigKey.ERROR_REPORTING_SENTRY_ENABLED)
        if (!enabled) return

        logger.info("Enabling error reporting")

        Sentry.init { options ->
            options.dsn = config.get(ConfigKey.ERROR_REPORTING_SENTRY_DSN)
        }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
