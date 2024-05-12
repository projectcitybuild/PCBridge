package com.projectcitybuild.pcbridge.support.spigot

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
) {
    fun register(vararg listeners: Listener) {
        listeners.forEach {
            plugin.server.pluginManager.registerSuspendingEvents(it, plugin)
        }
    }

    fun unregisterAll() {
        HandlerList.unregisterAll(plugin)
    }
}
