package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.entities.TeleportReason
import com.projectcitybuild.features.teleporthistory.repositories.TeleportHistoryRepository
import com.projectcitybuild.features.teleporting.events.PlayerTeleportEvent
import com.projectcitybuild.modules.config.PlatformConfig
import org.bukkit.event.EventHandler
import javax.inject.Inject

class PlayerTeleportListener @Inject constructor(
    private val teleportHistoryRepository: TeleportHistoryRepository,
    private val config: PlatformConfig,
): SpigotListener {

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val location = CrossServerLocation.fromLocation(
            serverName = config.get(PluginConfig.SPIGOT_SERVER_NAME),
            location = event.destination,
        )
        teleportHistoryRepository.add(
            event.player.uniqueId,
            location,
            TeleportReason.TP_PLAYER,
        )
    }
}