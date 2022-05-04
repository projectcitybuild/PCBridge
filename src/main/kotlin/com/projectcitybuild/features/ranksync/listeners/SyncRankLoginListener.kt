package com.projectcitybuild.features.ranksync.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import javax.inject.Inject

class SyncRankLoginListener @Inject constructor(
    private val updatePlayerGroupsUseCase: UpdatePlayerGroupsUseCase,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerJoin(event: PostLoginEvent)
        = updatePlayerGroupsUseCase.sync(event.player.uniqueId)
}
