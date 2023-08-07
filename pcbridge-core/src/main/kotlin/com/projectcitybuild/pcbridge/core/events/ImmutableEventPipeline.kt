package com.projectcitybuild.pcbridge.core.events

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.CoroutineContext

class ImmutableEventPipeline<T>(
    private val contextBuilder: () -> CoroutineContext,
) {
    private val channel = MutableSharedFlow<T>()

    suspend fun subscribe(collector: FlowCollector<T>) {
        contextBuilder().run {
            channel.collect(collector)
        }
    }

    suspend fun emit(value: T) {
        contextBuilder().run {
            channel.emit(value)
        }
    }
}