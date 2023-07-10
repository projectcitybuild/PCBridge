package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val server: Server,
) : SpigotListener<PlayerJoinEvent> {

    override suspend fun handle(event: PlayerJoinEvent) {
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
}
