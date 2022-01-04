package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class DonorPerkConnectionListener(
    private val permissionsManager: PermissionsManager,
    private val config: ConfigProvider,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val logger: LoggerProvider
): Listener {

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val donorAPI = apiRequestFactory.pcb.donorApi
        val response = apiClient.execute { donorAPI.getDonationTier(event.player.uniqueId.toString()) }

        when (response) {
            is APIResult.HTTPError -> return
            is APIResult.NetworkError -> return
            is APIResult.Success -> {
                val donationPerk = response.value.data?.first() ?: return
                val tierName = donationPerk.donationTier.name

                val configNode = "donors.tiers.$tierName.permission_group_name"
                val permissionGroupName = config.get(configNode) as? String
                    ?: throw Exception("No config node found for `$configNode`. Cannot assign donor tier group")

                val user = permissionsManager.getUser(event.player.uniqueId)
                    ?: throw Exception("Could not find Permissions user for ${event.player.name}")

                val permissionGroup = permissionsManager.getGroup(permissionGroupName)
                user.addGroup(permissionGroup)
                permissionsManager.saveChanges(user)

                logger.info("Assigned ${event.player.name} to donation tier group: $permissionGroupName")
            }
        }
    }
}