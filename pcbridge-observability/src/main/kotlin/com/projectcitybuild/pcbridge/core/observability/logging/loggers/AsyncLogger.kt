package com.projectcitybuild.pcbridge.core.observability.logging.loggers

interface AsyncLogger {
    suspend fun trace(event: Any?)
    suspend fun debug(event: Any?)
    suspend fun info(event: Any?)
    suspend fun warn(event: Any?)
    suspend fun error(event: Any?)
    suspend fun fatal(event: Any?)

    suspend fun trace(
        template: String,
        vararg values: Any?,
    )

    suspend fun debug(
        template: String,
        vararg values: Any?,
    )

    suspend fun info(
        template: String,
        vararg values: Any?,
    )

    suspend fun warn(
        template: String,
        vararg values: Any?,
    )

    suspend fun error(
        template: String,
        vararg values: Any?,
    )

    suspend fun fatal(
        template: String,
        vararg values: Any?,
    )

    suspend fun trace(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun debug(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun info(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun warn(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun error(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun fatal(
        throwable: Throwable,
        event: Any?,
    )

    suspend fun trace(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun debug(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun info(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun warn(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun error(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun fatal(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    suspend fun trace(event: suspend () -> Any?)
    suspend fun debug(event: suspend () -> Any?)
    suspend fun info(event: suspend () -> Any?)
    suspend fun warn(event: suspend () -> Any?)
    suspend fun error(event: suspend () -> Any?)
    suspend fun fatal(event: suspend () -> Any?)
}