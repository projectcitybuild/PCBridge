package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.GetGroupsForUUIDAction
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class SyncRankLoginListener(
        private val scheduler: SchedulerProvider,
        private val permissionsManager: PermissionsManager,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val groupsResult = GetGroupsForUUIDAction(apiRequestFactory, apiClient)
                .execute(playerId = event.player.uniqueId)

        val groupsForPlayer = if (groupsResult is Success) groupsResult.value else listOf()
        if (groupsForPlayer.isEmpty()) return

        val user = permissionsManager.getUser(event.player.uniqueId)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (uuid: ${event.player.uniqueId})")
            return
        }

        user.removeAllGroups()

        if (groupsForPlayer.isEmpty()) {
            // TODO: retrieve this from config instead
            val guestGroup = permissionsManager.getGroup("guest")
            user.addGroup(guestGroup)
            permissionsManager.saveChanges(user)
            return
        }

        groupsForPlayer.forEach { apiGroup ->
            if (apiGroup.minecraftName != null) {
                logger.info("Assigning to ${apiGroup.minecraftName} group")
                val group = permissionsManager.getGroup(apiGroup.minecraftName)
                user.addGroup(group)
            } else {
                logger.info("No group found for ${apiGroup.name}. Skipping...")
            }
        }
        permissionsManager.saveChanges(user)
    }
}