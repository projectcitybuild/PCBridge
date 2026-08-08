package com.projectcitybuild.pcbridge.core.observability.logging.loggers

import io.klogging.NoCoLogger

class KLoggingSyncLogger(
    private val logger: NoCoLogger,
) : SyncLogger {
    override fun trace(event: Any?) =
        logger.trace(event)

    override fun debug(event: Any?) =
        logger.debug(event)

    override fun info(event: Any?) =
        logger.info(event)

    override fun warn(event: Any?) =
        logger.warn(event)

    override fun error(event: Any?) =
        logger.error(event)

    override fun fatal(event: Any?) =
        logger.fatal(event)

    override fun trace(
        template: String,
        vararg values: Any?,
    ) = logger.trace(template, *values)

    override fun debug(
        template: String,
        vararg values: Any?,
    ) = logger.debug(template, *values)

    override fun info(
        template: String,
        vararg values: Any?,
    ) = logger.info(template, *values)

    override fun warn(
        template: String,
        vararg values: Any?,
    ) = logger.warn(template, *values)

    override fun error(
        template: String,
        vararg values: Any?,
    ) = logger.error(template, *values)

    override fun fatal(
        template: String,
        vararg values: Any?,
    ) = logger.fatal(template, *values)

    override fun trace(
        throwable: Throwable,
        event: Any?,
    ) = logger.trace(throwable, event)

    override fun debug(
        throwable: Throwable,
        event: Any?,
    ) = logger.debug(throwable, event)

    override fun info(
        throwable: Throwable,
        event: Any?,
    ) = logger.info(throwable, event)

    override fun warn(
        throwable: Throwable,
        event: Any?,
    ) = logger.warn(throwable, event)

    override fun error(
        throwable: Throwable,
        event: Any?,
    ) = logger.error(throwable, event)

    override fun fatal(
        throwable: Throwable,
        event: Any?,
    ) = logger.fatal(throwable, event)

    override fun trace(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.trace(throwable, template, *values)

    override fun debug(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.debug(throwable, template, *values)

    override fun info(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.info(throwable, template, *values)

    override fun warn(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.warn(throwable, template, *values)

    override fun error(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.error(throwable, template, *values)

    override fun fatal(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    ) = logger.fatal(throwable, template, *values)

    override fun trace(
        event: () -> Any?,
    ) = logger.trace { event() }

    override fun debug(
        event: () -> Any?,
    ) = logger.debug { event() }

    override fun info(
        event: () -> Any?,
    ) = logger.info { event() }

    override fun warn(
        event: () -> Any?,
    ) = logger.warn { event() }

    override fun error(
        event: () -> Any?,
    ) = logger.error { event() }

    override fun fatal(
        event: () -> Any?,
    ) = logger.fatal { event() }

    override fun trace(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.trace(throwable) { event() }

    override fun debug(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.debug(throwable) { event() }

    override fun info(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.info(throwable) { event() }

    override fun warn(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.warn(throwable) { event() }

    override fun error(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.error(throwable) { event() }

    override fun fatal(
        throwable: Throwable,
        event: () -> Any?,
    ) = logger.fatal(throwable) { event() }

    override fun trace(
        message: String,
        items: Map<String, Any?>,
    ) = logger.trace(message, items)

    override fun debug(
        message: String,
        items: Map<String, Any?>,
    ) = logger.debug(message, items)

    override fun info(
        message: String,
        items: Map<String, Any?>,
    ) = logger.info(message, items)

    override fun warn(
        message: String,
        items: Map<String, Any?>,
    ) = logger.warn(message, items)

    override fun error(
        message: String,
        items: Map<String, Any?>,
    ) = logger.error(message, items)

    override fun fatal(
        message: String,
        items: Map<String, Any?>,
    ) = logger.fatal(message, items)

    override fun trace(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.trace(message, throwable, items)

    override fun debug(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.debug(message, throwable, items)

    override fun info(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.info(message, throwable, items)

    override fun warn(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.warn(message, throwable, items)

    override fun error(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.error(message, throwable, items)

    override fun fatal(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    ) = logger.fatal(message, throwable, items)
}