package com.projectcitybuild.libs.errorreporting.outputs

import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.ConfigKeys
import com.projectcitybuild.libs.errorreporting.ErrorOutput
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import io.sentry.Sentry

class SentryErrorOutput(
    private val config: Config,
    private val logger: PlatformLogger,
) : ErrorOutput {

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
