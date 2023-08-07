package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class AnnounceQuitListener(
    private val server: Server,
) : SpigotListener<PlayerQuitEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerQuitEvent) {
        server.broadcastMessage(
            TextComponent()
<<<<<<< Updated upstream
                .add("— ") {
=======
                .add("- ") {
>>>>>>> Stashed changes
                    it.color = ChatColor.RED
                    it.isBold = true
                }
                .add(event.player.name) { it.color = ChatColor.WHITE }
                .add(" left the server") { it.color = ChatColor.GRAY }
                .toLegacyText()
        )
    }
}
