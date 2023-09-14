package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.modules.joinmessages.PlayerJoinTimeCache
import com.projectcitybuild.support.spigot.SpigotServer
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val server: SpigotServer,
    private val playerJoinTimeCache: PlayerJoinTimeCache,
) : SpigotListener<PlayerJoinEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerJoinEvent) {
        playerJoinTimeCache.put(event.player.uniqueId)

        server.broadcastMessage(
            TextComponent()
                .add("+ ") {
                    it.color = ChatColor.GREEN
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" joined the server") { it.color = ChatColor.GRAY }
        )
    }
}
