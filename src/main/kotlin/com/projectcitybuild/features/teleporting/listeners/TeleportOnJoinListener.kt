package com.projectcitybuild.features.teleporting.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.config.PluginConfig
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
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
        if (queuedTeleport.targetServerName != serverName) {
            return
        }

        logger.debug("Found queued warp request for $playerUUID -> $queuedTeleport")

        queuedTeleportRepository.dequeue(playerUUID)

        val destinationPlayer = event.player.server.getPlayer(queuedTeleport.targetPlayerUUID)
        if (destinationPlayer == null) {
            logger.warning("Could not find destination player. Did they disconnect?")
            return
        }

        event.spawnLocation = destinationPlayer.location

        logger.debug("Set player's spawn location to ${destinationPlayer.location}")

        when (queuedTeleport.teleportType) {
            TeleportType.TP -> {
                event.player.send().action("Teleported to ${destinationPlayer.name}")

                if (!queuedTeleport.isSilentTeleport) {
                    destinationPlayer.send().action("${event.player.name} teleported to you")
                }
            }
            TeleportType.SUMMON -> {
                destinationPlayer.send().action("You summoned ${event.player.name} to you")

                if (!queuedTeleport.isSilentTeleport) {
                    event.player.send().action("You were summoned to ${destinationPlayer.name}")
                }
            }
        }
    }
}