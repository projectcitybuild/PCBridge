package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
) {
    fun register(vararg listeners: Listener) {
        logSync.info { "Registering listeners" }
        listeners.forEach {
            logSync.debug { "Registering ${it::class.simpleName}" }
            plugin.server.pluginManager.registerSuspendingEvents(it, plugin)
        }
    }

    fun unregisterAll() {
        logSync.info { "Unregistered all listeners" }
        HandlerList.unregisterAll(plugin)
    }
}
