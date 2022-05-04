package com.projectcitybuild.features.ranksync.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import javax.inject.Inject

class SyncRankLoginListener @Inject constructor(
    private val updatePlayerGroupsUseCase: UpdatePlayerGroupsUseCase,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) =
        updatePlayerGroupsUseCase.sync(event.player.uniqueId)
}
