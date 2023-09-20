package com.projectcitybuild.pcbridge.core.architecture.features

import com.projectcitybuild.pcbridge.core.architecture.events.EventPipeline
import com.projectcitybuild.pcbridge.core.architecture.events.EventSink
import com.projectcitybuild.pcbridge.core.architecture.events.MiddlewareRegistry
import com.projectcitybuild.pcbridge.core.architecture.monitors.Monitorable
import com.projectcitybuild.pcbridge.core.architecture.monitors.MonitorableEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.NullEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.PluginDisableEvent
import com.projectcitybuild.pcbridge.core.architecture.monitors.ShutdownEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

abstract class PluginFeature(
    eventPipeline: EventPipeline,
    contextBuilder: () -> CoroutineContext,
): Monitorable {
    protected val events = EventSink(eventPipeline, contextBuilder)
    protected val middleware = MiddlewareRegistry(eventPipeline)

    override val monitorFlow: MutableStateFlow<MonitorableEvent> = MutableStateFlow(NullEvent)

    fun installOn(plugin: Monitorable) {
        plugin.monitor { event ->
            if (event is PluginDisableEvent || event is ShutdownEvent) {
                events.unsubscribe()
            }
        }
        onLoad()
    }

    abstract fun onLoad()
}