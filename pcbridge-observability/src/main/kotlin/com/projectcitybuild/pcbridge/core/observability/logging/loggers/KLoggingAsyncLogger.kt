package com.projectcitybuild.pcbridge.core.observability.logging.loggers

import io.klogging.Klogger

class KLoggingAsyncLogger(
    private val logger: Klogger,
) : AsyncLogger {
    override suspend fun trace(event: Any?) =
        logger.trace(event)

    override suspend fun debug(event: Any?) =
        logger.debug(event)

    override suspend fun info(event: Any?) =
        logger.info(event)

    override suspend fun warn(event: Any?) =
        logger.warn(event)

    override suspend fun error(event: Any?) =
        logger.error(event)

    override suspend fun fatal(event: Any?) =
        logger.fatal(event)

    override suspend fun trace(
        template: String,
        vararg values: Any?,
    ) = logger.trace(template, *values)

    override suspend fun debug(
        template: String,
        vararg values: Any?,
    ) = logger.debug(template, *values)

    override suspend fun info(
        template: String,
        vararg values: Any?,
    ) = logger.info(template, *values)

    override suspend fun warn(
        template: String,
        vararg values: Any?,
    ) = logger.warn(template, *values)

    override suspend fun error(
        template: String,
        vararg values: Any?,
    ) = logger.error(template, *values)

    override suspend fun fatal(
        template: String,
        vararg values: Any?,
    ) = logger.fatal(template, *values)

    override suspend fun trace(
        throwable: Throwable,
        event: Any?,
    ) = logger.trace(throwable, event)

    override suspend fun debug(
        throwable: Throwable,
        event: Any?,
    ) = logger.debug(throwable, event)

    override suspend fun info(
        throwable: Throwable,
        event: Any?,
    ) = logger.info(throwable, event)

    override suspend fun warn(
        throwable: Throwable,
        event: Any?,
    ) = logger.warn(throwable, event)

    override suspend fun error(
        throwable: Throwable,
        event: Any?,
    ) = logger.error(throwable, event)

    override suspend fun fatal(
        throwable: Throwable,
        event: Any?,
    ) = logger.fatal(throwable, event)

    override suspend fun trace(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.trace(throwable, template, *values)

    override suspend fun debug(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.debug(throwable, template, *values)

    override suspend fun info(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.info(throwable, template, *values)

    override suspend fun warn(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.warn(throwable, template, *values)

    override suspend fun error(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.error(throwable, template, *values)

    override suspend fun fatal(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.fatal(throwable, template, *values)

    override suspend fun trace(
        event: suspend () -> Any?,
    ) = logger.trace { event() }

    override suspend fun debug(
        event: suspend () -> Any?,
    ) = logger.debug { event() }

    override suspend fun info(
        event: suspend () -> Any?,
    ) = logger.info { event() }

    override suspend fun warn(
        event: suspend () -> Any?,
    ) = logger.warn { event() }

    override suspend fun error(
        event: suspend () -> Any?,
    ) = logger.error { event() }

    override suspend fun fatal(
        event: suspend () -> Any?,
    ) = logger.fatal { event() }
}