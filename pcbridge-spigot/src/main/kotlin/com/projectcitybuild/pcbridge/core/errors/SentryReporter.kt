package com.projectcitybuild.pcbridge.core.errors

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.logger.log
import io.sentry.Sentry

class SentryReporter(
    private val config: Config,
) {
    private var started = false

    fun start() {
        Sentry.init { options ->
            options.dsn = config.get().errorReporting.sentryDsn
        }
        started = true
        log.info { "Sentry error reporting enabled" }
    }

    fun close() {
        if (started) {
            Sentry.close()
            log.info { "Sentry error reporting disabled" }
        }
    }

    fun report(throwable: Throwable) {
        if (started) {
            Sentry.captureException(throwable)
        }
    }
}

suspend fun <R> SentryReporter.trace(block: suspend () -> R): Result<R> {
    return runCatching { block() }.onFailure {
        report(it)
        throw it
    }
}