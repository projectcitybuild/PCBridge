package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.plugin.events.PlayerPreLocationTeleportEvent
import com.projectcitybuild.plugin.events.PlayerPreSummonEvent
import com.projectcitybuild.plugin.events.PlayerPreTeleportEvent
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import org.bukkit.Location
import org.bukkit.event.EventHandler
import java.util.UUID
import javax.inject.Inject

class PlayerPreTeleportListener @Inject constructor(
    private val scheduler: PlatformScheduler,
    private val lastKnownLocationRepository: LastKnownLocationRepositoy,
    private val config: PlatformConfig,
) : SpigotListener {

    @EventHandler
    fun onPlayerPreSummon(event: PlayerPreSummonEvent) = rememberLocation(
        playerUUID = event.summonedPlayer.uniqueId,
        location = event.currentLocation,
    )

    @EventHandler
    fun onPlayerPreTeleport(event: PlayerPreTeleportEvent) = rememberLocation(
        playerUUID = event.player.uniqueId,
        location = event.currentLocation,
    )

    @EventHandler
    fun onPlayerWarp(event: PlayerPreLocationTeleportEvent) = rememberLocation(
        playerUUID = event.player.uniqueId,
        location = event.currentLocation,
    )

    private fun rememberLocation(playerUUID: UUID, location: Location) = scheduler.async<Unit> {
        lastKnownLocationRepository.set(
            playerUUID = playerUUID,
            location = SerializableLocation.fromLocation(
                serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME),
                location = location,
            )
        )
    }.start()
}
