package com.projectcitybuild.features.sync.repositories

import com.projectcitybuild.core.config.Config
import com.projectcitybuild.core.logger.log
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import java.util.UUID

class SyncRepository(
    private val playerGroupHttpService: PlayerGroupHttpService,
    private val accountLinkHttpService: AccountLinkHTTPService,
    private val config: Config,
) {
    @Throws(PlayerGroupHttpService.NoLinkedAccountException::class)
    suspend fun getGroups(playerUUID: UUID): List<String> {
        return playerGroupHttpService.getGroups(playerUUID)
            .mapNotNull { it.minecraftName }
    }

    @Throws(PlayerGroupHttpService.NoLinkedAccountException::class)
    suspend fun getDonorPerks(playerUUID: UUID): List<DonationPerk> {
        return playerGroupHttpService.getDonorPerks(playerUUID)
    }

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

    @Throws(AccountLinkHTTPService.AlreadyLinkedException::class)
    suspend fun generateVerificationURL(playerUUID: UUID): String? {
        return accountLinkHttpService.generateVerificationURL(playerUUID)
    }
}
