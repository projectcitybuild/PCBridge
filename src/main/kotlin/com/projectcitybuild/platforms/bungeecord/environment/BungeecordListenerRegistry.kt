package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.modules.logger.LoggerProvider
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.event.HandlerList

class BungeecordListenerRegistry constructor(
        private val plugin: Plugin,
        private val logger: LoggerProvider
) {

    fun register(listener: Listener) {
        logger.verbose("Beginning listener registration...")

        plugin.proxy.pluginManager?.registerListener(plugin, listener).let {
            logger.verbose("Registered listener: ${listener::class}")
        }
    }

    fun unregisterAll() {
        HandlerList.unregisterAll()
        logger.verbose("Unregistered all listeners")
    }
}