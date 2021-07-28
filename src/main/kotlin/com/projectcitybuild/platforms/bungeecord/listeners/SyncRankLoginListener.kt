package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.ranks.GetGroupsForUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordScheduler
import com.projectcitybuild.platforms.bungeecord.permissions.PermissionsManager
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class SyncRankLoginListener(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val scheduler: BungeecordScheduler,
    private val permissionsManager: PermissionsManager,
    private val logger: BungeecordLogger
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {
        GetGroupsForUUIDAction(apiRequestFactory, apiClient).execute(
            playerId = event.player.uniqueId
        ) { result ->
            val groupsForPlayer = if (result is Success) result.value else listOf()
            if (groupsForPlayer.isEmpty()) return@execute

            scheduler.sync {
                val user = permissionsManager.getUser(event.player.uniqueId)
                if (user == null) {
                    logger.warning("Could not load user from permissions manager (uuid: ${event.player.uniqueId})")
                    return@sync
                }

                user.removeAllGroups()

                if (groupsForPlayer.isEmpty()) {
                    // TODO: retrieve this from config instead
                    val guestGroup = permissionsManager.getGroup("guest")
                    user.addGroup(guestGroup)
                    permissionsManager.saveChanges(user)
                    return@sync
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
    }
}