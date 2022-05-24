package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.textcomponentbuilder.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import javax.inject.Inject

class ServerJoinMessageListener @Inject constructor(
    private val server: Server,
) : SpigotListener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        server.broadcastMessage(
            TextComponent()
                .add("+ ") {
                    it.color = ChatColor.GREEN
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" joined the server") { it.color = ChatColor.GRAY }
                .toLegacyText()
        )
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        server.broadcastMessage(
            TextComponent()
                .add("— ") {
                    it.color = ChatColor.RED
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" left the server") { it.color = ChatColor.GRAY }
                .toLegacyText()
        )
    }
}
