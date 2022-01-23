package com.projectcitybuild.features.joinmessage.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.features.joinmessage.events.FirstTimeJoinEvent
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.event.EventHandler
import javax.inject.Inject

class FirstTimeJoinMessageListener @Inject constructor(
    private val proxyServer: ProxyServer
): BungeecordListener {

    @EventHandler
    fun onFirstTimeJoin(event: FirstTimeJoinEvent) {
        proxyServer.broadcast(
            TextComponent()
                .add("✦ Welcome ") {
                    it.color = ChatColor.LIGHT_PURPLE
                }
                .add(event.player.name) {
                    it.color = ChatColor.WHITE
                }
                .add(" to the server!") {
                    it.color = ChatColor.LIGHT_PURPLE
                }
        )
    }
}