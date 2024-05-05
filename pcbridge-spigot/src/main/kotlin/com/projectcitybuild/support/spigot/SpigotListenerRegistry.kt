package com.projectcitybuild.support.spigot

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
) {
    fun register(listener: Listener) {
        plugin.server.pluginManager.registerSuspendingEvents(listener, plugin)
    }

    fun register(vararg listeners: Listener) {
        listeners.forEach(::register)
    }

    fun unregisterAll() {
        HandlerList.unregisterAll(plugin)
    }
}