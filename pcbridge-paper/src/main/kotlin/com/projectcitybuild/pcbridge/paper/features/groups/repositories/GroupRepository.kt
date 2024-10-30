package com.projectcitybuild.pcbridge.paper.features.groups.repositories

import com.projectcitybuild.pcbridge.paper.core.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.logger.log
import com.projectcitybuild.pcbridge.http.models.DonationPerk

class GroupRepository(
    private val localConfig: LocalConfig,
) {
    fun getDonorTiers(perks: List<DonationPerk>): List<String> {
        return perks.mapNotNull { donorPerk ->
            val tierName = donorPerk.donationTier.name

            val groupNames = localConfig.get().groups.donorTierGroupNames
            when (tierName) {
                "copper" -> groupNames.copper
                "iron" -> groupNames.iron
                "diamond" -> groupNames.diamond
                else -> {
                    log.error { "Missing config node for donor tier: $tierName" }
                    null
                }
            }
        }
    }
}
