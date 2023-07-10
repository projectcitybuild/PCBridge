package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val server: Server,
) : SpigotListener<PlayerJoinEvent> {

    override suspend fun handle(event: PlayerJoinEvent) {
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
