package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import javax.inject.Inject

class WelcomeMessageListener @Inject constructor(
    private val server: Server
) : SpigotListener {

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val onlinePlayerCount = server.onlinePlayers.size

        event.player.spigot().sendMessage(
            TextComponent().add(
                TextComponent.fromLegacyText(
                    """
                    #§3Welcome to §f§lPROJECT §6§lCITY §9§lBUILD
                    #
                    #§3Type §c/register §3to become a member.
                    #§3Type §c/list§6 §3to see who else is online.
                    #§3Players online:§c $onlinePlayerCount
                    #
                    #§f§lAsk our staff if you have any questions.
                """.trimMargin("#")
                )
            )
        )
    }
}
