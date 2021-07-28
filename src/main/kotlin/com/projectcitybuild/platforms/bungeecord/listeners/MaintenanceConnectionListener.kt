package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.PluginConfig
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class MaintenanceConnectionListener(
        private val config: ConfigProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: LoginEvent) {
        val isMaintenanceModeOn = config.get(PluginConfig.SETTINGS.MAINTENANCE_MODE)

        if (isMaintenanceModeOn) {
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
}