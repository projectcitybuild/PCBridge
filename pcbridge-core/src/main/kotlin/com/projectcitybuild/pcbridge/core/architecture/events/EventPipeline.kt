package com.projectcitybuild.pcbridge.core.architecture.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class EventPipeline(
    private val contextBuilder: () -> CoroutineContext,
) {
    private val flows: MutableMap<Class<*>, MutableStateFlow<*>> = mutableMapOf()
    private val middlewares: MutableMap<Class<*>, MutableList<EventMiddleware<*>>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    private fun <T> castedFlow(clazz: Class<T>): MutableStateFlow<T?> {
        return flows
            .getOrPut(clazz) { MutableStateFlow<T?>(null) }
            as MutableStateFlow<T?>
    }

    fun <T> flowForEvent(clazz: Class<T>): Flow<T> {
        return castedFlow(clazz)
            .asStateFlow()
            .filterNotNull()
    }

    fun <T: Any> emit(event: T) {
        val guards = middlewares[event.javaClass] ?: emptyList()

        CoroutineScope(contextBuilder()).launch {
            if (guards.isEmpty()) {
                castedFlow(event.javaClass).tryEmit(event)
            } else {
                val result = withContext(Dispatchers.IO) {
                    for (guard in guards) {
                        @Suppress("UNCHECKED_CAST")
                        val middleware = guard as EventMiddleware<T>
                        if (middleware.process(event) == MiddlewareResult.reject) {
                            return@withContext MiddlewareResult.reject
                        }
                    }
                    MiddlewareResult.allow
                }
                if (result == MiddlewareResult.allow) {
                    castedFlow(event.javaClass).tryEmit(event)
                }
            }
        }
    }

    fun <T> guardEvent(clazz: Class<T>, middleware: EventMiddleware<T>) {
        middlewares
            .getOrPut(clazz) { mutableListOf() }
            .add(middleware)
    }

    fun <T> unguardEvent(clazz: Class<T>, middleware: EventMiddleware<T>) {
        middlewares[clazz]?.remove(middleware)
    }
}
