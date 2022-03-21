package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

@Reusable
class BungeecordListenerRegistry @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger
) {

    fun register(listener: Listener) {
        logger.verbose("Beginning listener registration...")

        plugin.proxy.pluginManager?.registerListener(plugin, listener).let {
            logger.verbose("Registered listener: ${listener::class}")
        }
    }

    fun unregisterAll() {
        plugin.proxy.pluginManager?.unregisterListeners(plugin)
        logger.verbose("Unregistered all listeners")
    }
}
