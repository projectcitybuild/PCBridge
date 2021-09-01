package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BanConnectionListener(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        val action = CheckBanStatusAction(apiRequestFactory, apiClient)
        val result = action.execute(playerId = event.uniqueId)

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
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    textComponent.toLegacyText()
            )
        }
    }
}