package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class OpClearListener(
    private val plugin: JavaPlugin,
    private val server: Server,
) : Listener {
    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        if (event.plugin != plugin) return

        event.tracedSync(pimTracer, this::class.java) {
            server.operators.forEach { it.isOp = false }
        }
    }
}
