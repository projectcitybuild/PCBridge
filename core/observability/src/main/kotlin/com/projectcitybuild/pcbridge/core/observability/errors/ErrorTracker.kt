package com.projectcitybuild.pcbridge.core.observability.errors

import kotlinx.coroutines.CancellationException

class ErrorTracker(
    private val sentry: SentryProvider,
) {
    fun report(throwable: Throwable) = sentry.report(throwable)
}

fun ErrorTracker.reportSafely(throwable: Throwable) {
    try {
        report(throwable)
    } catch (reportingFailure: Throwable) {
        if (reportingFailure !== throwable) {
            throwable.addSuppressed(reportingFailure)
        }
    }
}

suspend fun <R> ErrorTracker.catching(block: suspend () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        reportSafely(e)
        Result.failure(e)
    }
}
