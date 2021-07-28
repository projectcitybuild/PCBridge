package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.platforms.bungeecord.permissions.PermissionsManager
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class MaintenanceConnectionListener(
        private val config: ConfigProvider,
        private val permissionsManager: PermissionsManager
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: LoginEvent) {
        val isMaintenanceModeOn = config.get(PluginConfig.SETTINGS.MAINTENANCE_MODE)

        if (!isMaintenanceModeOn) return

        // FIXME: this won't work if their rank isn't synced first...
        val user = permissionsManager.getUser(event.connection.uniqueId)
        if (user != null && user.hasPermission("pcbridge.maintenance.bypass")) {
            return
        }

        val textComponent = TextComponent().apply {
            this.addExtra(TextComponent("Server maintenance in progress\n\n").apply {
                this.color = ChatColor.GOLD
                this.isBold = true
            })
            this.addExtra(TextComponent("We'll be back shortly!").apply {
                this.color = ChatColor.WHITE
            })
        }
        event.setCancelReason(textComponent)
        event.isCancelled = true
    }
}