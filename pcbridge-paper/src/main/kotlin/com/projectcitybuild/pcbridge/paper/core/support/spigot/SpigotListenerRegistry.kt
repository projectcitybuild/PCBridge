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
        logSync.debug { "Registering listeners" }

        val registered = mutableListOf<String>()
        listeners.forEach {
            val name = it::class.simpleName ?: it::class.java.simpleName
            logSync.trace { "Registering $name..." }
            plugin.server.pluginManager.registerSuspendingEvents(it, plugin)
            registered.add(name)
        }
        logSync.info("Listeners registered", mapOf(
            "listeners" to registered,
        ))
    }

    fun unregisterAll() {
        logSync.info { "Unregistered all listeners" }
        HandlerList.unregisterAll(plugin)
    }
}
