package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BanConnectionListener(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: LoginEvent) {
        val action = CheckBanStatusAction(apiRequestFactory, apiClient)
        val result = action.executeSynchronously(playerId = event.connection.uniqueId)

        if (result is Success && result.value != null) {
            val ban = result.value
            val textComponent = TextComponent().apply {
                this.addExtra(TextComponent("You are currently banned.\n\n").apply {
                    this.color = ChatColor.RED
                    this.isBold = true
                })
                this.addExtra(TextComponent("Reason: ").apply {
                    this.color = ChatColor.GRAY
                })
                this.addExtra(TextComponent((ban.reason ?: "No reason provided") + "\n").apply {
                    this.color = ChatColor.WHITE
                })
                this.addExtra(TextComponent("Expires: ").apply {
                    this.color = ChatColor.GRAY
                })
                this.addExtra(TextComponent(ban.expiresAt?.toString() ?: "Never" + "\n\n").apply {
                    this.color = ChatColor.WHITE
                })
                this.addExtra(TextComponent("Appeal @ https://projectcitybuild.com").apply {
                    this.color = ChatColor.AQUA
                })
            }
            event.setCancelReason(textComponent)
            event.isCancelled = true
        }
    }
}