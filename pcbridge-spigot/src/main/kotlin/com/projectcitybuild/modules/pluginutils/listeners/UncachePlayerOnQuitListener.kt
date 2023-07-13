package com.projectcitybuild.modules.pluginutils.listeners

import com.projectcitybuild.modules.chat.ChatGroupFormatter
import com.projectcitybuild.libs.playercache.PlayerConfigCache
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class UncachePlayerOnQuitListener(
    private val playerCache: PlayerConfigCache,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val chatBadgeRepository: ChatBadgeRepository,
) : SpigotListener<PlayerQuitEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerQuitEvent) {
        val player = event.player

        playerCache.remove(uuid = player.uniqueId)
        chatGroupFormatter.flush(playerUUID = player.uniqueId)
        chatBadgeRepository.remove(playerUUID = player.uniqueId)
    }
}
