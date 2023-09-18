package com.projectcitybuild.pcbridge.core.architecture.monitors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

interface Monitorable {
    val monitorFlow: MutableStateFlow<MonitorableEvent>

    fun emit(event: MonitorableEvent) {
        monitorFlow.tryEmit(event)
    }

    fun monitor(handler: suspend (MonitorableEvent) -> Unit) {
        CoroutineScope(Job()).launch {
            monitorFlow
                .filterNotNull()
                .collect { event -> handler(event) }
        }
    }
}