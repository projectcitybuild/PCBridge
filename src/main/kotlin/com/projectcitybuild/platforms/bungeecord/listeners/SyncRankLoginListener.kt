package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.bungeecord.extensions.async
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class SyncRankLoginListener(
    private val syncPlayerGroupAction: SyncPlayerGroupAction
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {
        async {
            syncPlayerGroupAction.execute(event.player.uniqueId)
        }
    }
}