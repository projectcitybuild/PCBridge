package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class SyncRankLoginListener(
    private val syncPlayerGroupAction: SyncPlayerGroupAction
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            syncPlayerGroupAction.execute(event.player.uniqueId)
        }
    }
}