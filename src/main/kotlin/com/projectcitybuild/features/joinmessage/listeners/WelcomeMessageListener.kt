package com.projectcitybuild.features.joinmessage.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import javax.inject.Inject

class WelcomeMessageListener @Inject constructor(
    private val proxyServer: ProxyServer
) : BungeecordListener {

    @EventHandler
    fun onPostLoginEvent(event: PostLoginEvent) {
        val onlinePlayerCount = proxyServer.players.size

        event.player.sendMessage(
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
