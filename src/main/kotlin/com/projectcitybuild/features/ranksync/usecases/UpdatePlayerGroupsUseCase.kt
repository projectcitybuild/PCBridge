package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.responses.Group
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.permissions.PermissionsManager
import java.util.*
import javax.inject.Inject

class UpdatePlayerGroupsUseCase @Inject constructor(
    private val permissionsManager: PermissionsManager,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val config: PlatformConfig,
    private val logger: PlatformLogger
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
        PERMISSION_USER_NOT_FOUND,
    }

    suspend fun sync(playerUUID: UUID): Result<Unit, FailureReason> {
        val response = try {
            val authAPI = apiRequestFactory.pcb.authApi
            apiClient.execute { authAPI.getUserGroups(uuid = playerUUID.toString()) }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "account_not_linked") {
                return Failure(FailureReason.ACCOUNT_NOT_LINKED)
            }
            throw e
        }

        val groups: List<Group> = response.data?.groups ?: listOf()

        val user = permissionsManager.getUser(playerUUID)
        if (user == null) {
            logger.warning("Could not load user from permissions manager (UUID: ${playerUUID})")
            return Failure(FailureReason.PERMISSION_USER_NOT_FOUND)
        }

        user.removeAllGroups()

        if (groups.isEmpty()) {
            val guestGroupName = config.get(PluginConfig.GROUPS_GUEST)
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