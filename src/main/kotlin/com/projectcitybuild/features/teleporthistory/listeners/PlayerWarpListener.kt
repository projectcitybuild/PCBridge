package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.integrations.shared.crossteleport.events.PlayerPreLocationTeleportEvent
import com.projectcitybuild.modules.config.PlatformConfig
import org.bukkit.event.EventHandler
import javax.inject.Inject

class PlayerWarpListener @Inject constructor(
    private val lastKnownLocationRepository: LastKnownLocationRepositoy,
    private val config: PlatformConfig,
    ): SpigotListener {

    @EventHandler
    fun onPlayerWarp(event: PlayerPreLocationTeleportEvent) {
        lastKnownLocationRepository.set(
            playerUUID = event.player.uniqueId,
            location = CrossServerLocation.fromLocation(
                serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME),
                location = event.currentLocation,
            )
        )
    }
}