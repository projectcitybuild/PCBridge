package com.projectcitybuild.support.spigot.listeners

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerRegistry(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger
) {
    fun <T: Event> register(listener: SpigotListener<T>) {
        class AnonymousListener<T: Event>(
            private val handler: suspend (T) -> Unit,
        ): Listener {
            @EventHandler(priority = handlerPriority)
            suspend fun handle(event: T) = handler(event)
        }
        val handler = AnonymousListener(listener::handle)
        plugin.server.pluginManager.registerSuspendingEvents(handler, plugin)

        logger.verbose("Registered listener ${listener::class.simpleName}")
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}
