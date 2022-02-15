package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.features.teleporthistory.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.modules.config.PlatformConfig
import org.bukkit.event.EventHandler
import javax.inject.Inject

class PlayerWarpListener @Inject constructor(
    private val lastKnownLocationRepository: LastKnownLocationRepositoy,
    private val config: PlatformConfig,
    ): SpigotListener {

    @EventHandler
    fun onPlayerWarp(event: PlayerPreWarpEvent) {
        lastKnownLocationRepository.set(
            playerUUID = event.player.uniqueId,
            location = CrossServerLocation.fromLocation(
                serverName = config.get(ConfigKeys.SPIGOT_SERVER_NAME),
                location = event.currentLocation,
            )
        )
    }
}