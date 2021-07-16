package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.entities.Group
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.TrustGroup
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.ranks.GetGroupsForUUIDAction
import com.projectcitybuild.modules.ranks.PermissionGroupFactory
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

            scheduler.sync {
                val user = permissionsManager.getUser(event.player.uniqueId)
                if (user == null) {
                    logger.warning("Could not load user from permissions manager (uuid: ${event.player.uniqueId})")
                    return@sync
                }

                user.removeAllGroups()

                if (groupsForPlayer.isEmpty()) {
                    val guestGroupName = PermissionGroupFactory().fromGroup(Group.TRUST(TrustGroup.GUEST))
                    val guestGroup = permissionsManager.getGroup(guestGroupName!!) // FIXME
                    user.addGroup(guestGroup)
                    permissionsManager.saveChanges(user)
                    return@sync
                }

                groupsForPlayer.forEach { apiGroupName ->
                    val mapper = PermissionGroupFactory()
                    val permissionGroup = mapper.fromAPIGroup(apiGroupName.name)

                    if (permissionGroup == null) {
                        logger.warning("Unable to find group model for API group: ${apiGroupName.name}")
                        return@forEach
                    }

                    val permissionGroupName = mapper.fromGroup(permissionGroup)
                    if (permissionGroupName == null) {
                        logger.warning("Unable to find permission plugin group for group model: ${permissionGroup.toString()}")
                        return@forEach
                    }

                    val group = permissionsManager.getGroup(permissionGroupName)
                    user.addGroup(group)
                }

                permissionsManager.saveChanges(user)
            }
        }
    }
}