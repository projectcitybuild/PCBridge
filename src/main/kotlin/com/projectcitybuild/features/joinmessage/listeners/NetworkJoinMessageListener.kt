package com.projectcitybuild.features.joinmessage.listeners

import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class NetworkJoinMessageListener(
    private val proxyServer: ProxyServer
): Listener {

    @EventHandler
    fun onPostLoginEvent(event: PostLoginEvent) {
        proxyServer.broadcast(
            TextComponent()
                .add("+ ") {
                    it.color = ChatColor.GREEN
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" joined the server") { it.color = ChatColor.GRAY }
        )
    }

    @EventHandler
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        proxyServer.broadcast(
            TextComponent()
                .add("— ") {
                    it.color = ChatColor.RED
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" left the server") { it.color = ChatColor.GRAY }
        )
    }

    @EventHandler
    fun onServerSwitchEvent(event: ServerSwitchEvent) {
        // Joining the network also sends a ServerSwitchEvent
        if (event.from == null) return

        proxyServer.broadcast(
            TextComponent()
                .add("⇒ ") {
                    it.color = ChatColor.YELLOW
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" switched to ") { it.color = ChatColor.GRAY }
                .add(event.player.server.info.name) { it.color = ChatColor.WHITE }
        )
    }
}