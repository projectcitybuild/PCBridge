package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.config.ConfigStorageKey
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import java.util.UUID

class PlayerGroupRepository(
    private val playerGroupHttpService: PlayerGroupHttpService,
    private val config: Config,
    private val logger: PlatformLogger,
) {
    @Throws(PlayerGroupHttpService.NoLinkedAccountException::class)
    suspend fun getGroups(playerUUID: UUID): List<String> {
        return playerGroupHttpService.getGroups(playerUUID)
            .mapNotNull { it.minecraftName }
    }

    @Throws(PlayerGroupHttpService.NoLinkedAccountException::class)
    suspend fun getDonorTiers(playerUUID: UUID): List<String> {
        val perks = playerGroupHttpService.getDonorPerks(playerUUID)

        return perks.mapNotNull { donorPerk ->
            val tierName = donorPerk.donationTier.name

            val configNode = ConfigStorageKey<String?>(
                path = "donors.tiers.$tierName.permission_group_name",
                defaultValue = null
            )
            val permissionGroupName = config.get(configNode)

            if (permissionGroupName == null) {
                logger.severe("Missing config node for donor tier: $tierName")
                return@mapNotNull null
            }

            return@mapNotNull permissionGroupName
        }
    }
}
