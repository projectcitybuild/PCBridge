package com.projectcitybuild.integrations.internal.crossteleport.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.integrations.internal.crossteleport.CrossServerTeleportQueue
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import javax.inject.Inject

class TeleportOnJoinListener @Inject constructor(
    private val crossServerTeleportQueue: CrossServerTeleportQueue,
    private val logger: PlatformLogger,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerSpawnLocationEvent) {
        val playerUUID = event.player.uniqueId
        val result = crossServerTeleportQueue.dequeue(playerUUID)

        when (result) {
            is Failure -> when (result.reason) {
                is CrossServerTeleportQueue.FailureReason.WorldNotFound -> {
                    event.player.send().error("Could not find ${result.reason.worldName} world")
                    logger.warning("Could not find ${result.reason.worldName} world to warp to")
                }
                is CrossServerTeleportQueue.FailureReason.DestinationPlayerNotFound -> {
                }
            }
            is Success -> {
                val queuedTeleport = result.value
                if (queuedTeleport == null) {
                    logger.debug("No queued teleport for $playerUUID")
                    return
                }

                logger.debug("Found queued teleport for $playerUUID")

                event.spawnLocation = when (queuedTeleport) {
                    is CrossServerTeleportQueue.Destination.Location -> {
                        // TODO: update string
                        event.player.send().action("Warped to ${queuedTeleport.name}")

                        queuedTeleport.location
                    }
                    is CrossServerTeleportQueue.Destination.Player -> {
                        val destinationPlayer = queuedTeleport.destinationPlayer

                        if (queuedTeleport.isSummon) {
                            destinationPlayer.send().action("You summoned ${event.player.name} to you")

                            if (!queuedTeleport.isSilentTeleport) {
                                event.player.send().action("You were summoned to ${destinationPlayer.name}")
                            }
                        } else {
                            event.player.send().action("Teleported to ${destinationPlayer.name}")

                            if (!queuedTeleport.isSilentTeleport) {
                                destinationPlayer.send().action("${event.player.name} teleported to you")
                            }
                        }
                        queuedTeleport.location
                    }
                }
            }
        }
    }
}
