package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.core.contracts.LoggerProvider
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotListenerDelegate constructor(
        private val plugin: WeakReference<JavaPlugin>,
        private val logger: LoggerProvider
    ) {

    fun register(listener: Listener) {
        val plugin = plugin.get() ?: throw Exception("Failed to register listener: Plugin is deallocated")
        logger.verbose("Beginning listener registration...")

        plugin.server?.pluginManager?.registerEvents(listener, plugin).let {
            logger.verbose("Registered listener: ${listener::class}")
        }
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}