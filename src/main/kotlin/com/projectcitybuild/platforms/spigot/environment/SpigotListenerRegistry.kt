package com.projectcitybuild.platforms.spigot.environment

import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.projectcitybuild.core.contracts.LoggerProvider
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry constructor(
        private val plugin: JavaPlugin,
        private val logger: LoggerProvider
    ) {

    fun register(listener: Listener) {
        logger.verbose("Beginning listener registration...")

        plugin.server.pluginManager.registerSuspendingEvents(listener, plugin)
        logger.verbose("Registered listener: ${listener::class.simpleName}")

//        plugin.server?.pluginManager?.registerEvents(listener, plugin).let {
//            logger.verbose("Registered listener: ${listener::class}")
//        }
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}