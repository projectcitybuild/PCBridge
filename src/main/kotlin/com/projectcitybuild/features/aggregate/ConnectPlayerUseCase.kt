package com.projectcitybuild.features.aggregate

import com.projectcitybuild.entities.IPBan
import com.projectcitybuild.entities.responses.Aggregate
import com.projectcitybuild.entities.responses.GameBan
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.repositories.AggregateRepository
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.repositories.IPBanRepository
import java.util.UUID
import javax.inject.Inject

class ConnectPlayerUseCase @Inject constructor(
    private val permissions: Permissions,
    private val aggregateRepository: AggregateRepository,
    private val ipBanRepository: IPBanRepository,
    private val chatBadgeRepository: ChatBadgeRepository,
    private val config: Config,
    private val logger: PlatformLogger,
) {
    sealed class ConnectResult {
        object Allowed : ConnectResult()
        object Failed : ConnectResult()
        data class Denied(val ban: Ban) : ConnectResult()
    }

    sealed class Ban {
        data class UUID(val value: GameBan) : Ban()
        data class IP(val value: IPBan) : Ban()
    }

    @Throws(Exception::class)
    suspend fun execute(playerUUID: UUID, ip: String): ConnectResult {
        val aggregate = aggregateRepository.get(playerUUID = playerUUID)
            ?: return ConnectResult.Failed

        val ban = getBan(ip, aggregate)
        if (ban != null) {
            return ConnectResult.Denied(ban = ban)
        }

        syncGroups(playerUUID, aggregate)
        chatBadgeRepository.put(playerUUID, aggregate.badges)

        return ConnectResult.Allowed
    }

    private fun getBan(ip: String, aggregate: Aggregate): Ban? {
        if (aggregate.ban != null) {
            return Ban.UUID(aggregate.ban)
        }
        val sanitizedIP = Sanitizer().sanitizedIP(ip)
        val ipBan = ipBanRepository.get(sanitizedIP)
        if (ipBan != null) {
            return Ban.IP(ipBan)
        }
        return null
    }

    private fun syncGroups(playerUUID: UUID, aggregate: Aggregate) {
        if (aggregate.account == null) {
            return
        }
        val groupSet = mutableSetOf<String>()
        groupSet.addAll(
            aggregate.account.groups.mapNotNull { it.minecraftName }
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
}
