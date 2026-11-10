package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class WorldStartupListener(
    private val plugin: JavaPlugin,
    private val lifecycleService: WorldLifecycleService,
) : Listener {
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        lifecycleService.list()
            .filterNot { it.isLoaded }
            .forEach { registration ->
                val name = registration.definition.name
                try {
                    lifecycleService.load(name)
                    plugin.logger.info("Auto-loaded registered world $name")
                } catch (failure: Exception) {
                    plugin.logger.log(Level.SEVERE, "Could not auto-load registered world $name", failure)
                }
            }
    }
}
