package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.logger.PlatformLogger
import java.util.UUID
import javax.inject.Inject

class PlayerGroupRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val config: Config,
    private val logger: PlatformLogger,
) {
    class AccountNotLinkedException : Exception()

    @Throws(AccountNotLinkedException::class)
    suspend fun getGroups(playerUUID: UUID): List<String> {
        val response = try {
            val authAPI = apiRequestFactory.pcb.authAPI
            apiClient.execute { authAPI.getUserGroups(uuid = playerUUID.toString()) }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "account_not_linked") {
                throw AccountNotLinkedException()
            }
            throw e
        }

        return response.data?.groups
            ?.mapNotNull { it.minecraftName }
            ?: listOf()
    }

    @Throws(AccountNotLinkedException::class)
    suspend fun getDonorTiers(playerUUID: UUID): List<String> {
        val response = try {
            val donorAPI = apiRequestFactory.pcb.donorAPI
            apiClient.execute { donorAPI.getDonationTier(playerUUID.toString()) }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "account_not_linked") {
                throw AccountNotLinkedException()
            }
            throw e
        }

        return response.data?.mapNotNull { donorPerk ->
            val tierName = donorPerk.donationTier.name

            val configNode = ConfigStorageKey<String?>(
                path = "donors.tiers.$tierName.permission_group_name",
                defaultValue = null
            )
            val permissionGroupName = config.get(configNode)

            if (permissionGroupName == null) {
                logger.fatal("Missing config node for donor tier: $tierName")
                return@mapNotNull null
            }

            return@mapNotNull permissionGroupName
        } ?: emptyList()
    }
}
