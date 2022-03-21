package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.infrastructure.network.APIClient
import com.projectcitybuild.core.infrastructure.network.APIRequestFactory
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.Group
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.permissions.Permissions
import java.util.*
import javax.inject.Inject

class UpdatePlayerGroupsUseCase @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val permissions: Permissions,
    private val config: PlatformConfig,
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

        val groupNames = groups
            .mapNotNull { it.minecraftName }
            .ifEmpty { listOf(config.get(ConfigKey.GROUPS_GUEST)) }

        permissions.setUserGroups(playerUUID, groupNames)

        return Success(Unit)
    }
}
