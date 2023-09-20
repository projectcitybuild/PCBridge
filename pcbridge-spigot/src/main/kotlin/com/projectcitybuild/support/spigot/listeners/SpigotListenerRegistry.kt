package com.projectcitybuild.support.spigot.listeners

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

@Deprecated("Use EventPipeline instead")
class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger
) {
    fun <T: Event> register(listener: SpigotListener<T>) {
        plugin.server.pluginManager.registerSuspendingEvents(listener, plugin)
        logger.verbose("Registered listener ${listener::class.simpleName}")
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}