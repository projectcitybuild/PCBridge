package com.projectcitybuild.pcbridge.paper.core.libs.errors

import com.projectcitybuild.pcbridge.paper.core.libs.errors.destinations.NopReportDestination
import com.projectcitybuild.pcbridge.paper.core.libs.errors.destinations.SentryReportDestination
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig

class ErrorReporter(
    private val localConfig: LocalConfig,
) {
    private var destination: ReportDestination = NopReportDestination()

    suspend fun start() {
        val config = localConfig.get()
        if (config.errorReporting.isSentryEnabled) {
            destination = SentryReportDestination(
                dsn = config.errorReporting.sentryDsn,
            )
        }
        destination.start()
    }

    fun close() = destination.close()

    fun report(throwable: Throwable) = destination.report(throwable)
}

suspend fun <R> ErrorReporter.trace(block: suspend () -> R): Result<R> {
    return runCatching { block() }.onFailure {
        report(it)
        throw it
    }
}
