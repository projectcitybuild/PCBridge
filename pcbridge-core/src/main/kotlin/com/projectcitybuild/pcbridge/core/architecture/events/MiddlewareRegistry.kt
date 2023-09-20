package com.projectcitybuild.pcbridge.core.architecture.events

class MiddlewareRegistry(
    private val eventPipeline: EventPipeline,
) {
    fun <T> guardEvent(clazz: Class<T>, middleware: EventMiddleware<T>)
        = eventPipeline.guardEvent(clazz, middleware)

    fun <T> unguardEvent(clazz: Class<T>, middleware: EventMiddleware<T>)
        = eventPipeline.unguardEvent(clazz, middleware)
}