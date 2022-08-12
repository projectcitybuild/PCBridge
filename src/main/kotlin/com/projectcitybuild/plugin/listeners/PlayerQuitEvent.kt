package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import com.projectcitybuild.support.textcomponent.add
import com.projectcitybuild.repositories.ChatBadgeRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import javax.inject.Inject

class PlayerQuitEvent @Inject constructor(
    private val server: Server,
    private val playerCache: PlayerConfigCache,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val chatBadgeRepository: ChatBadgeRepository,
) : SpigotListener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        announceLeave(player = event.player)
        uncachePlayer(player = event.player)
    }

    private fun announceLeave(player: Player) {
        server.broadcastMessage(
            TextComponent()
                .add("— ") {
                    it.color = ChatColor.RED
                    it.isBold = true
                }
                .add(player.name) { it.color = ChatColor.WHITE }
                .add(" left the server") { it.color = ChatColor.GRAY }
                .toLegacyText()
        )
    }

    private fun uncachePlayer(player: Player) {
        playerCache.remove(uuid = player.uniqueId)
        chatGroupFormatter.flush(playerUUID = player.uniqueId)
        chatBadgeRepository.remove(playerUUID = player.uniqueId)
    }
}
