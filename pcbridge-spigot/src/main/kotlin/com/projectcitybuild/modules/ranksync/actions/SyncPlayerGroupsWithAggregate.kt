package com.projectcitybuild.modules.ranksync.actions

import com.projectcitybuild.libs.config.Config
import com.projectcitybuild.libs.config.ConfigStorageKey
import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import java.util.UUID

class SyncPlayerGroupsWithAggregate(
    private val permissions: Permissions,
    private val config: Config,
    private val logger: PlatformLogger,
) {
    fun execute(playerUUID: UUID, aggregate: Aggregate) {
        val account = aggregate.account ?: return

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
                    logger.severe("Missing config node for donor tier: $tierName")
                    return@mapNotNull null
                }

                return@mapNotNull permissionGroupName
            }
        )
        permissions.setUserGroups(playerUUID, groupSet.toList())
    }
}
