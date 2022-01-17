package com.projectcitybuild.features.teleporting.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import javax.inject.Inject

class TeleportOnJoinListener @Inject constructor(
    private val queuedTeleportRepository: QueuedTeleportRepository,
    private val config: PlatformConfig,
    private val logger: PlatformLogger,
): SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val playerUUID = event.player.uniqueId
        val serverName = config.get(PluginConfig.SPIGOT_SERVER_NAME)

        val queuedTeleport = queuedTeleportRepository.get(playerUUID)
        if (queuedTeleport == null) {
            logger.debug("No queued teleport for $playerUUID")
            return
        }
        if (queuedTeleport.targetServerName == serverName) {
            logger.debug("Found queued warp request for $playerUUID -> $queuedTeleport")

            queuedTeleportRepository.dequeue(playerUUID)

            val destinationPlayer = event.player.server.getPlayer(queuedTeleport.targetPlayerUUID)
            if (destinationPlayer == null) {
                logger.warning("Could not find destination player. Did they disconnect?")
                return
            }

            event.spawnLocation = destinationPlayer.location

            logger.debug("Set player's spawn location to ${destinationPlayer.location}")
        }
    }
}