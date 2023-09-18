package com.projectcitybuild.pcbridge.core.architecture.events

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class EventSink(
    private val eventPipeline: EventPipeline,
    private val contextBuilder: () -> CoroutineContext,
) {
    private val jobs: MutableMap<Class<*>, MutableList<Job>> = mutableMapOf()

    fun <T> subscribe(
        clazz: Class<T>,
        handler: suspend (event: T) -> Unit,
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throw throwable
        }
        val minecraftContext = contextBuilder()
        val job = CoroutineScope(Job() + Dispatchers.Default + exceptionHandler).launch {
            eventPipeline.flowForEvent(clazz)
                .filterNotNull()
                .collect { event ->
                    withContext(minecraftContext) {
                        handler(event)
                    }
                }
        }

        jobs
            .getOrPut(clazz) { mutableListOf() }
            .add(job)
    }

    fun <T> unsubscribe(clazz: Class<T>) {
        val handlers = jobs.remove(clazz)

        handlers?.forEach { job ->
            job.cancel()
        }
    }

    fun unsubscribe() {
        jobs.values.forEach { handlers ->
            handlers.forEach { job ->
                job.cancel()
            }
        }
        jobs.clear()
    }
}