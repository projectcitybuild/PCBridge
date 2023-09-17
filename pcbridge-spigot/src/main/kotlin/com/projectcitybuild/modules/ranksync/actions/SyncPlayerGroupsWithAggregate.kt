package com.projectcitybuild.modules.ranksync.actions

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import java.util.UUID

class SyncPlayerGroupsWithAggregate(
    private val permissions: Permissions,
    private val config: Config<ConfigData>,
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

                val groupNames = config.get().groups.donorTierGroupNames
                val permissionGroupName = when (tierName) {
                    "copper" -> groupNames.copper
                    "iron" -> groupNames.iron
                    "diamond" -> groupNames.diamond
                    else -> {
                        logger.severe("Missing config node for donor tier: $tierName")
                        return@mapNotNull null
                    }
                }
                return@mapNotNull permissionGroupName
            }
        )
        permissions.setUserGroups(playerUUID, groupSet.toList())
    }
}
