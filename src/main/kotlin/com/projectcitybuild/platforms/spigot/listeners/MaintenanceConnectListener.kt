package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.core.entities.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerLoginEvent

class MaintenanceConnectListener(
        private val environment: EnvironmentProvider
) : Listenable<PlayerLoginEvent> {

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: PlayerLoginEvent) {
        val isMaintenanceMode = environment.get(PluginConfig.Settings.MAINTENANCE_MODE()) as? Boolean
            ?: throw Exception("Cannot cast MAINTENANCE_MODE value to Boolean")

        if (event.player.hasPermission("pcbridge.maintenance.bypass")) {
            return
        }
        if (isMaintenanceMode) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Server is currently in maintenance mode. Please try again later")
        }
    }
}