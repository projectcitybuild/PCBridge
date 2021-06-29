package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BanConnectionListener(
        private val environment: EnvironmentProvider,
        private val networkClients: NetworkClients
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: PreLoginEvent) {
        val action = CheckBanStatusAction(networkClients)
        val result = action.execute(playerId = event.connection.uniqueId)

        if (result is CheckBanStatusAction.Result.SUCCESS && result.ban != null) {
            val textComponent = TextComponent().apply {
                this.addExtra(TextComponent("You are currently banned.\n\n").apply {
                    this.color = ChatColor.RED
                    this.isBold = true
                })
                this.addExtra(TextComponent("Reason: ").apply {
                    this.color = ChatColor.GRAY
                })
                this.addExtra(TextComponent(result.ban.reason ?: "No reason provided" + "\n\n").apply {
                    this.color = ChatColor.WHITE
                })
                this.addExtra(TextComponent("Expires: ").apply {
                    this.color = ChatColor.GRAY
                })
                this.addExtra(TextComponent(result.ban.expiresAt?.toString() ?: "Never" + "\n\n").apply {
                    this.color = ChatColor.WHITE
                })
                this.addExtra(TextComponent("Appeal @ https://projectcitybuild.com").apply {
                    this.color = ChatColor.AQUA
                })
            }
            event.setCancelReason(textComponent)
        }
    }
}