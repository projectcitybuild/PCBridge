package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.http.APIClient
import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.Group
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.Permissions
import java.util.UUID
import javax.inject.Inject

class UpdatePlayerGroupsUseCase @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val permissions: Permissions,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
) {
    enum class FailureReason {
        ACCOUNT_NOT_LINKED,
        PERMISSION_USER_NOT_FOUND,
    }

    suspend fun sync(playerUUID: UUID): Result<Unit, FailureReason> {
        val groupSet = mutableSetOf<String>()

        val groupResult = syncGroups(playerUUID)
        if (groupResult is Success) {
            groupSet.addAll(groupResult.value)
        }

        val tierResult = syncDonorTier(playerUUID)
        if (tierResult is Success) {
            groupSet.addAll(tierResult.value)
        }

        permissions.setUserGroups(playerUUID, groupSet.toList())

        return when (groupResult) {
            is Success -> Success(Unit)
            is Failure -> Failure(groupResult.reason)
        }
    }

    private suspend fun syncGroups(playerUUID: UUID): Result<List<String>, FailureReason> {
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

        return Success(groupNames)
    }

    private suspend fun syncDonorTier(playerUUID: UUID): Result<List<String>, FailureReason> {
        val response = try {
            val donorAPI = apiRequestFactory.pcb.donorApi
            apiClient.execute { donorAPI.getDonationTier(playerUUID.toString()) }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "account_not_linked") {
                return Failure(FailureReason.ACCOUNT_NOT_LINKED)
            }
            throw e
        }

        val groupNames = response.data?.mapNotNull { donorPerk ->
            val tierName = donorPerk.donationTier.name

            val configNode = "donors.tiers.$tierName.permission_group_name"
            val permissionGroupName = config.get(configNode) as? String

            if (permissionGroupName == null) {
                logger.fatal("Missing config node for donor tier: $tierName")
                return@mapNotNull null
            }

            return@mapNotNull permissionGroupName
        } ?: emptyList()

        return Success(groupNames)
    }
}
