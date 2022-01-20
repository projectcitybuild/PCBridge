package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.TeleportReason
import com.projectcitybuild.features.teleporthistory.repositories.TeleportHistoryRepository
import com.projectcitybuild.features.warps.events.PlayerWarpEvent
import org.bukkit.event.EventHandler
import javax.inject.Inject

class PlayerWarpListener @Inject constructor(
    private val teleportHistoryRepository: TeleportHistoryRepository,
): SpigotListener {

    @EventHandler
    fun onPlayerWarp(event: PlayerWarpEvent) {
        teleportHistoryRepository.add(
            event.player.uniqueId,
            event.warp.location,
            TeleportReason.WARP,
        )
    }
}