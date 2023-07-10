package com.projectcitybuild.support.spigot.listeners

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger
) {
    fun <T: Event> register(listener: SpigotListener<T>) {
        val handler = when(listener.priority) {
            EventPriority.MONITOR -> MonitorPriorityListener(listener::handle)
            EventPriority.HIGHEST -> HighestPriorityListener(listener::handle)
            EventPriority.HIGH -> HighPriorityListener(listener::handle)
            EventPriority.NORMAL -> NormalPriorityListener(listener::handle)
            EventPriority.LOW -> LowPriorityListener(listener::handle)
            EventPriority.LOWEST -> LowestPriorityListener(listener::handle)
        }
        plugin.server.pluginManager.registerSuspendingEvents(handler, plugin)

        logger.verbose("Registered listener ${listener::class.simpleName} (priority: ${listener.priority})")
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}

private class HighestPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun handle(event: T) = handler(event)
}

private class HighPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.HIGH)
    suspend fun handle(event: T) = handler(event)
}

private class NormalPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun handle(event: T) = handler(event)
}

private class LowPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.LOW)
    suspend fun handle(event: T) = handler(event)
}

private class LowestPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun handle(event: T) = handler(event)
}

private class MonitorPriorityListener<T: Event>(
    private val handler: suspend (T) -> Unit,
): Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun handle(event: T) = handler(event)
}