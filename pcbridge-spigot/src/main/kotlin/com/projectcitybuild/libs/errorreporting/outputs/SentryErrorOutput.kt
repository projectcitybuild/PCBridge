package com.projectcitybuild.libs.errorreporting.outputs

import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.ConfigData
import com.projectcitybuild.libs.errorreporting.ErrorOutput
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import io.sentry.Sentry

class SentryErrorOutput(
    private val config: Config<ConfigData>,
    private val logger: PlatformLogger,
) : ErrorOutput {

    override fun start() {
        val isEnabled = config.get().errorReporting.isSentryEnabled
        if (!isEnabled) return

        logger.info("Enabling error reporting")

        Sentry.init { options ->
            options.dsn = config.get().errorReporting.sentryDsn
        }
    }

    override fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
