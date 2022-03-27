package com.projectcitybuild.features.teleporthistory.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.features.teleporting.events.PlayerPreSummonEvent
import com.projectcitybuild.features.teleporting.events.PlayerPreTeleportEvent
import com.projectcitybuild.integrations.internal.crossteleport.events.PlayerPreLocationTeleportEvent
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import org.bukkit.Location
import org.bukkit.event.EventHandler
import java.util.UUID
import javax.inject.Inject

class TeleportListener @Inject constructor(
    private val lastKnownLocationRepository: LastKnownLocationRepositoy,
    private val config: PlatformConfig,
) : SpigotListener {

    @EventHandler
    fun onPreLocationTeleport(event: PlayerPreLocationTeleportEvent) {
        rememberLastLocation(
            playerUUID = event.player.uniqueId,
            location = event.currentLocation,
        )
    }

    @EventHandler
    fun onPrePlayerSummon(event: PlayerPreSummonEvent) {
        rememberLastLocation(
            playerUUID = event.summonedPlayer.uniqueId,
            location = event.currentLocation,
        )
    }

    @EventHandler
    fun onPrePlayerTeleport(event: PlayerPreTeleportEvent) {
        rememberLastLocation(
            playerUUID = event.player.uniqueId,
            location = event.currentLocation,
        )
    }

    private fun rememberLastLocation(playerUUID: UUID, location: Location) {
        lastKnownLocationRepository.set(
            playerUUID = playerUUID,
            location = CrossServerLocation.fromLocation(
                serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME),
                location = location,
            )
        )
    }
}
