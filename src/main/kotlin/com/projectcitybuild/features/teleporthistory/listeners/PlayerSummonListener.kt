package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.TeleportReason
import com.projectcitybuild.features.teleporthistory.repositories.TeleportHistoryRepository
import com.projectcitybuild.features.teleporting.events.PlayerSummonEvent
import com.projectcitybuild.modules.config.PlatformConfig
import org.bukkit.event.EventHandler
import javax.inject.Inject

class PlayerSummonListener @Inject constructor(
    private val teleportHistoryRepository: TeleportHistoryRepository,
    private val config: PlatformConfig,
): SpigotListener {

    @EventHandler
    fun onPlayerSummon(event: PlayerSummonEvent) {
        val location = CrossServerLocation.fromLocation(
            serverName = config.get(PluginConfig.SPIGOT_SERVER_NAME),
            location = event.destination,
        )
        teleportHistoryRepository.add(
            event.summonedPlayer.uniqueId,
            location,
            TeleportReason.TP_SUMMON,
        )
    }
}