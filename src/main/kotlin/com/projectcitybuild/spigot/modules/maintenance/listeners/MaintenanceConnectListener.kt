package com.projectcitybuild.spigot.modules.maintenance.listeners

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.entities.models.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerLoginEvent

class MaintenanceConnectListener : Listenable<PlayerLoginEvent> {
    override var environment: Environment? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: PlayerLoginEvent) {
        val environment = environment ?: return

        val isMaintenanceMode = environment.get(PluginConfig.Settings.MAINTENANCE_MODE()) as? Boolean
            ?: throw Exception("Cannot cast MAINTENANCE_MODE value to Boolean")

        if (isMaintenanceMode) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Server is currently in maintenance mode. Please try again later")
        }
    }
}