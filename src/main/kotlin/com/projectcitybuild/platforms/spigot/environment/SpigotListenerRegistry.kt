package com.projectcitybuild.platforms.spigot.environment

import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

@Reusable
class SpigotListenerRegistry @Inject constructor(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger
) {
    fun register(listener: Listener) {
        logger.verbose("Beginning listener registration...")

        plugin.server.pluginManager.registerSuspendingEvents(listener, plugin)
        logger.verbose("Registered listener: ${listener::class.simpleName}")
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}
