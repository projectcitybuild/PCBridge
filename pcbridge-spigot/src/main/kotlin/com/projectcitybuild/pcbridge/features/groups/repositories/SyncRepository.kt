package com.projectcitybuild.pcbridge.features.groups.repositories

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.http.responses.DonationPerk

class SyncRepository(
    private val config: Config,
) {
    fun getDonorTiers(perks: List<DonationPerk>): List<String> {
        return perks.mapNotNull { donorPerk ->
            val tierName = donorPerk.donationTier.name

            val groupNames = config.get().groups.donorTierGroupNames
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
