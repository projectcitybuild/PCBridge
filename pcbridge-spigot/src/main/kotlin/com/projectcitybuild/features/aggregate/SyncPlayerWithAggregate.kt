package com.projectcitybuild.features.aggregate

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.support.spigot.logger.Logger
import java.util.UUID

class SyncPlayerWithAggregate(
    private val permissions: Permissions,
    private val chatBadgeRepository: ChatBadgeRepository,
    private val config: Config,
    private val logger: Logger,
) {
    fun execute(playerUUID: UUID, aggregate: Aggregate) {
        syncGroups(playerUUID, aggregate)
        syncBadges(playerUUID, aggregate)
    }

    private fun syncGroups(playerUUID: UUID, aggregate: Aggregate) {
        val account = aggregate.account ?: return;

        val groupSet = mutableSetOf<String>()
        groupSet.addAll(
            account.groups.mapNotNull { it.minecraftName }
        )
        groupSet.addAll(
            aggregate.donationPerks.mapNotNull { donorPerk ->
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
            }
        )
        permissions.setUserGroups(playerUUID, groupSet.toList())
    }

    private fun syncBadges(playerUUID: UUID, aggregate: Aggregate) {
        chatBadgeRepository.put(playerUUID, aggregate.badges)
    }
}
