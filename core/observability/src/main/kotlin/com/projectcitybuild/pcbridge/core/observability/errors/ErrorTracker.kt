package com.projectcitybuild.pcbridge.core.observability.errors

import kotlinx.coroutines.CancellationException

class ErrorTracker(
    private val sentry: SentryProvider,
) {
    fun report(throwable: Throwable) = sentry.report(throwable)

    fun reportSafely(throwable: Throwable) {
        try {
            report(throwable)
        } catch (reportingFailure: Throwable) {
            if (reportingFailure !== throwable) {
                throwable.addSuppressed(reportingFailure)
            }
        }
    }

    suspend fun <R> catching(block: suspend () -> R): Result<R> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        reportSafely(e)
        Result.failure(e)
    }
}
