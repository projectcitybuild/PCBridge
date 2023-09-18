package com.projectcitybuild.pcbridge.core.architecture.monitors

sealed interface MonitorableEvent

object NullEvent: MonitorableEvent

object ShutdownEvent: MonitorableEvent
object PluginLoadEvent: MonitorableEvent
object PluginEnableEvent: MonitorableEvent
object PluginDisableEvent: MonitorableEvent