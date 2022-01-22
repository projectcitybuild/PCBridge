package com.projectcitybuild.features.teleporting

import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.QueuedTeleport
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ServerConnectEvent
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerTeleportRequester @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val queuedTeleportRepository: QueuedTeleportRepository,
) {
    fun teleport(player: ProxiedPlayer, destinationPlayer: ProxiedPlayer, shouldCheckAllowingTP: Boolean) {
        if (shouldCheckAllowingTP) {
            val targetPlayerConfig = playerConfigRepository.get(destinationPlayer.uniqueId)!!
            if (!targetPlayerConfig.isAllowingTPs) {
                player.send().error("${destinationPlayer.name} is disallowing teleports")
                return
            }
        }

        val destinationServer = destinationPlayer.server.info
        val isDestinationPlayerOnSameServer = player.server.info.name == destinationServer.name
        if (isDestinationPlayerOnSameServer) {
            MessageToSpigot(
                destinationServer,
                SubChannel.TP_SAME_SERVER,
                arrayOf(
                    player.uniqueId.toString(),
                    destinationPlayer.uniqueId.toString(),
                    false,
                )
            ).send()
        } else {
            queuedTeleportRepository.queue(
                QueuedTeleport(
                    playerUUID = player.uniqueId,
                    targetPlayerUUID = destinationPlayer.uniqueId,
                    targetServerName = destinationServer.name,
                    teleportType = TeleportType.TP,
                    createdAt = LocalDateTime.now()
                )
            )
            MessageToSpigot(
                player.server.info,
                SubChannel.TP_ACROSS_SERVER,
                arrayOf(
                    player.uniqueId.toString(),
                    destinationServer.name,
                )
            ).send()
        }
    }

    fun summon(summonedPlayer: ProxiedPlayer, destinationPlayer: ProxiedPlayer, shouldCheckAllowingTP: Boolean) {
        if (shouldCheckAllowingTP) {
            val summonedPlayerConfig = playerConfigRepository.get(summonedPlayer.uniqueId)!!
            if (!summonedPlayerConfig.isAllowingTPs) {
                destinationPlayer.send().error("${summonedPlayer.name} is disallowing teleports")
                return
            }
        }

        val targetServer = destinationPlayer.server.info
        val isTargetPlayerOnSameServer = summonedPlayer.server.info.name == targetServer.name
        if (isTargetPlayerOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.TP_SAME_SERVER,
                arrayOf(
                    summonedPlayer.uniqueId.toString(),
                    destinationPlayer.uniqueId.toString(),
                    true,
                )
            ).send()
        } else {
            queuedTeleportRepository.queue(
                QueuedTeleport(
                    playerUUID = summonedPlayer.uniqueId,
                    targetPlayerUUID = destinationPlayer.uniqueId,
                    targetServerName = targetServer.name,
                    teleportType = TeleportType.TP,
                    createdAt = LocalDateTime.now()
                )
            )
            MessageToSpigot(
                summonedPlayer.server.info,
                SubChannel.TP_ACROSS_SERVER,
                arrayOf(
                    summonedPlayer.uniqueId.toString(),
                    targetServer.name,
                )
            ).send()
        }
    }
}