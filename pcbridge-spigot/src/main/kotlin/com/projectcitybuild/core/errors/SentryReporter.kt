package com.projectcitybuild.core.errors

import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import io.sentry.Sentry

class SentryReporter(
    private val config: Config<PluginConfig>,
    private val logger: PlatformLogger,
) {
    private var started = false

    fun start() {
        Sentry.init { options ->
            options.dsn = config.get().errorReporting.sentryDsn
        }
        started = true
        logger.info("Sentry error reporting enabled")
    }

    fun close() {
        if (started) {
            Sentry.close()
            logger.info("Sentry error reporting disabled")
        }
    }

    fun report(throwable: Throwable) {
        if (started) {
            Sentry.captureException(throwable)
        }
    }
}
