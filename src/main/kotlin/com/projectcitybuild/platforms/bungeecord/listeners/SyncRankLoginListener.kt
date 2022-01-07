package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.modules.ranks.SyncPlayerGroupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class SyncRankLoginListener(
    private val syncPlayerGroupService: SyncPlayerGroupService
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            syncPlayerGroupService.execute(event.player.uniqueId)
        }
    }
}