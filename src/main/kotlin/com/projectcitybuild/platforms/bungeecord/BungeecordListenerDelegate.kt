package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.entities.LogLevel
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.event.HandlerList

class BungeecordListenerDelegate constructor(
        private val plugin: Plugin,
        private val environment: EnvironmentProvider
) {

    fun register(listener: Listener) {
        environment.log(LogLevel.VERBOSE, "Beginning listener registration...")

        plugin.proxy.pluginManager?.registerListener(plugin, listener).let {
            environment.log(LogLevel.VERBOSE, "Registered listener: ${listener::class}")
        }
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        environment.log(LogLevel.VERBOSE, "Unregistered all listeners")
    }
}