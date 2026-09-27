package com.projectcitybuild.pcbridge.paper.core.libs.observability.errors

class ErrorTracker(
    private val sentry: SentryProvider,
) {
    fun report(throwable: Throwable) = sentry.report(throwable)
}

suspend fun <R> ErrorTracker.catching(block: suspend () -> R): Result<R> {
    return runCatching { block() }.onFailure {
        report(it)
        throw it
    }
}
