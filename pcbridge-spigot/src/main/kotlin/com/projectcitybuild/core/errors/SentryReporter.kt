package com.projectcitybuild.core.errors

import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.core.config.PluginConfig
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import io.sentry.Sentry

class SentryReporter(
    private val config: Config<PluginConfig>,
    private val logger: PlatformLogger,
) {
    fun start() {
        val isEnabled = config.get().errorReporting.isSentryEnabled
        if (!isEnabled) return

        logger.info("Sentry error reporting enabled")

        Sentry.init { options ->
            options.dsn = config.get().errorReporting.sentryDsn
        }
    }

    fun report(throwable: Throwable) {
        Sentry.captureException(throwable)
    }
}
