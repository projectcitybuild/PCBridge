package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class OpClearListener(
    private val plugin: JavaPlugin,
    private val server: Server,
): Listener {
    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        if (event.plugin != plugin) return

        event.scopedSync(opElevateTracer, this::class.java) {
            server.operators.forEach { it.isOp = false }
        }
    }
}