package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import java.util.*

class SyncPlayerGroupAction(
        private val permissionsManager: PermissionsManager,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val config: ConfigProvider,
        private val logger: LoggerProvider
) {
    suspend fun execute(playerUUID: UUID): Result<Unit, Unit> {
        val groupsResult = GetGroupsForUUIDAction(apiRequestFactory, apiClient)
                .execute(playerId = playerUUID)

        val groupsForPlayer = if (groupsResult is Success) groupsResult.value else listOf()

        val user = permissionsManager.getUser(playerUUID)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (UUID: ${playerUUID})")
            return Failure(Unit)
        }

        user.removeAllGroups()

        if (groupsForPlayer.isEmpty()) {
            val guestGroupName = config.get(PluginConfig.GROUPS.GUEST)
            val guestGroup = permissionsManager.getGroup(guestGroupName)
            user.addGroup(guestGroup)
            permissionsManager.saveChanges(user)
            logger.info("User has no assigned groups. Assigning to $guestGroupName group")
            return Success(Unit)
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

        return Success(Unit)
    }
}