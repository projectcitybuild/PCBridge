package com.projectcitybuild.features.ranksync.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import javax.inject.Inject

class SyncRankLoginListener @Inject constructor(
    private val updatePlayerGroupsUseCase: UpdatePlayerGroupsUseCase,
) : BungeecordListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            updatePlayerGroupsUseCase.sync(event.player.uniqueId)
        }
    }
}
