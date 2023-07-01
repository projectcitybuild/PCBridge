package com.projectcitybuild.support.spigot.listeners

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger
) {
    fun register(listener: Listener) {
        plugin.server.pluginManager.registerSuspendingEvents(listener, plugin)
        logger.verbose("Registered listener: ${listener::class.simpleName}")
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}
