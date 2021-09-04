package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class MaintenanceConnectListener(
        private val config: ConfigProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerLoginEvent(event: PlayerLoginEvent) {
        val isMaintenanceMode = config.get(PluginConfig.SETTINGS.MAINTENANCE_MODE)

        if (event.player.hasPermission("pcbridge.maintenance.bypass")) {
            return
        }
        if (isMaintenanceMode) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Server is currently in maintenance mode. Please try again later")
        }
    }
}