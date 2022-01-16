package com.projectcitybuild.features.ranksync

import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.responses.Group
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.permissions.PermissionsManager
import java.util.*
import javax.inject.Inject

class SyncPlayerGroupService @Inject constructor(
    private val permissionsManager: PermissionsManager,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val config: ConfigProvider,
    private val logger: LoggerProvider
) {
    class AccountNotLinkedException: Exception()
    class PermissionUserNotFoundException: Exception()

    suspend fun execute(playerUUID: UUID) {
        val response = try {
            val authAPI = apiRequestFactory.pcb.authApi
            apiClient.execute { authAPI.getUserGroups(uuid = playerUUID.toString()) }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "account_not_linked") {
                throw AccountNotLinkedException()
            }
            throw e
        }

        var groups: List<Group> = response.data?.groups ?: listOf()

        val user = permissionsManager.getUser(playerUUID)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (UUID: ${playerUUID})")
            throw PermissionUserNotFoundException()
        }

        user.removeAllGroups()

        if (groups.isEmpty()) {
            val guestGroupName = config.get(PluginConfig.GROUPS_GUEST)
            val guestGroup = permissionsManager.getGroup(guestGroupName)
            user.addGroup(guestGroup)
            permissionsManager.saveChanges(user)
            logger.info("User has no assigned groups. Assigning to $guestGroupName group")
            return
        }

        groups.forEach { apiGroup ->
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