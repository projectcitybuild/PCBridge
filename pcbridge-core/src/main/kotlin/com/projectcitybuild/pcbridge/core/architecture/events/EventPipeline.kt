package com.projectcitybuild.pcbridge.core.architecture.events

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class EventPipeline {
    private val flows: MutableMap<Class<*>, MutableStateFlow<*>> = mutableMapOf()

    fun <T> flowForEvent(clazz: Class<T>): Flow<T> {
        return mutableFlowForEvent(clazz)
            .asStateFlow()
            .filterNotNull()
    }

    fun <T: Any> emit(event: T) {
        mutableFlowForEvent(event.javaClass)
            .tryEmit(event)
    }

    private fun <T> mutableFlowForEvent(clazz: Class<T>): MutableStateFlow<T?> {
        return flows.getOrPut(clazz) {
            MutableStateFlow<T?>(null)
        } as MutableStateFlow<T?>
    }
}