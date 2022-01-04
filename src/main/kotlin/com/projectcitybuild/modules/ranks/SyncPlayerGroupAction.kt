package com.projectcitybuild.modules.ranks

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.Success
import com.projectcitybuild.entities.Result
import com.projectcitybuild.entities.models.ApiError
import com.projectcitybuild.entities.models.Group
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import java.util.*

class SyncPlayerGroupAction(
        private val permissionsManager: PermissionsManager,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val config: ConfigProvider,
        private val logger: LoggerProvider
) {
    sealed class FailReason {
        class HTTPError(error: ApiError?): FailReason()
        object NetworkError: FailReason()
        object AccountNotLinked: FailReason()
        object PermissionUserNotFound: FailReason()
    }

    suspend fun execute(playerUUID: UUID): Result<Unit, FailReason> {
        val authAPI = apiRequestFactory.pcb.authApi
        val response = apiClient.execute { authAPI.getUserGroups(uuid = playerUUID.toString()) }

        var groups: List<Group>

        when (response) {
            is APIResult.Success -> groups = response.value.data?.groups ?: listOf()
            is APIResult.NetworkError -> return Failure(FailReason.NetworkError)
            is APIResult.HTTPError -> {
                if (response.error?.id == "account_not_linked") {
                    return Failure(FailReason.AccountNotLinked)
                }
                return Failure(FailReason.HTTPError(response.error))
            }
        }

        val user = permissionsManager.getUser(playerUUID)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (UUID: ${playerUUID})")
            return Failure(FailReason.PermissionUserNotFound)
        }

        user.removeAllGroups()

        if (groups.isEmpty()) {
            val guestGroupName = config.get(PluginConfig.GROUPS.GUEST)
            val guestGroup = permissionsManager.getGroup(guestGroupName)
            user.addGroup(guestGroup)
            permissionsManager.saveChanges(user)
            logger.info("User has no assigned groups. Assigning to $guestGroupName group")
            return Success(Unit)
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

        return Success(Unit)
    }
}