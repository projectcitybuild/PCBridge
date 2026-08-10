package com.projectcitybuild.pcbridge.core.observability.logging.loggers

interface SyncLogger {
    fun trace(event: Any?)
    fun debug(event: Any?)
    fun info(event: Any?)
    fun warn(event: Any?)
    fun error(event: Any?)
    fun fatal(event: Any?)

    fun trace(
        template: String,
        vararg values: Any?,
    )

    fun debug(
        template: String,
        vararg values: Any?,
    )

    fun info(
        template: String,
        vararg values: Any?,
    )

    fun warn(
        template: String,
        vararg values: Any?,
    )

    fun error(
        template: String,
        vararg values: Any?,
    )

    fun fatal(
        template: String,
        vararg values: Any?,
    )

    fun trace(
        throwable: Throwable,
        event: Any?,
    )

    fun debug(
        throwable: Throwable,
        event: Any?,
    )

    fun info(
        throwable: Throwable,
        event: Any?,
    )

    fun warn(
        throwable: Throwable,
        event: Any?,
    )

    fun error(
        throwable: Throwable,
        event: Any?,
    )

    fun fatal(
        throwable: Throwable,
        event: Any?,
    )

    fun trace(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun debug(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun info(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun warn(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun error(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun fatal(
        throwable: Throwable,
        template: String,
        vararg values: Any?,
    )

    fun trace(event: () -> Any?)
    fun debug(event: () -> Any?)
    fun info(event: () -> Any?)
    fun warn(event: () -> Any?)
    fun error(event: () -> Any?)
    fun fatal(event: () -> Any?)

    fun trace(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun debug(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun info(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun warn(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun error(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun fatal(
        throwable: Throwable,
        event: () -> Any?,
    )

    fun trace(
        message: String,
        items: Map<String, Any?>,
    )

    fun debug(
        message: String,
        items: Map<String, Any?>,
    )

    fun info(
        message: String,
        items: Map<String, Any?>,
    )

    fun warn(
        message: String,
        items: Map<String, Any?>,
    )

    fun error(
        message: String,
        items: Map<String, Any?>,
    )

    fun fatal(
        message: String,
        items: Map<String, Any?>,
    )

    fun trace(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )

    fun debug(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )

    fun info(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )

    fun warn(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )

    fun error(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )

    fun fatal(
        message: String,
        throwable: Throwable,
        items: Map<String, Any?>,
    )
}