package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class SyncRankLoginListener(
        private val syncPlayerGroupAction: SyncPlayerGroupAction
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        syncPlayerGroupAction.execute(event.player.uniqueId)
    }
}