package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.GetGroupsForUUIDAction
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class SyncRankLoginListener(
        private val syncPlayerGroupAction: SyncPlayerGroupAction
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        syncPlayerGroupAction.execute(event.player.uniqueId)
    }
}