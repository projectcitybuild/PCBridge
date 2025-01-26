package com.projectcitybuild.pcbridge.paper.core.libs.errors

import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import io.sentry.Sentry

class SentryReporter(
    private val localConfig: LocalConfig,
) {
    private var started = false

    fun start() {
        Sentry.init { options ->
            options.dsn = localConfig.get().errorReporting.sentryDsn
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
