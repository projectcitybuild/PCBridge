package com.projectcitybuild.features.teleporting

import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Teleport
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ServerConnectEvent
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerTeleporter @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val queuedTeleportRepository: QueuedTeleportRepository,
) {
    fun teleport(player: ProxiedPlayer, targetPlayer: ProxiedPlayer, shouldCheckAllowingTP: Boolean) {
        if (shouldCheckAllowingTP) {
            val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)!!
            if (!targetPlayerConfig.isAllowingTPs) {
                player.send().error("${targetPlayer.name} is disallowing teleports")
                return
            }
        }

        val targetServer = targetPlayer.server.info
        val isTargetPlayerOnSameServer = player.server.info.name == targetServer.name
        if (isTargetPlayerOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.TP_IMMEDIATELY,
                arrayOf(
                    player.uniqueId.toString(),
                    targetPlayer.uniqueId.toString(),
                )
            ).send()
        } else {
            queuedTeleportRepository.queue(
                Teleport(
                    playerUUID = player.uniqueId,
                    targetPlayerUUID = targetPlayer.uniqueId,
                    targetServerName = targetServer.name,
                    teleportType = TeleportType.TP,
                    createdAt = LocalDateTime.now()
                )
            )
            player.connect(targetServer, ServerConnectEvent.Reason.COMMAND)
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
                SubChannel.TP_IMMEDIATELY,
                arrayOf(
                    summonedPlayer.uniqueId.toString(),
                    destinationPlayer.uniqueId.toString(),
                )
            ).send()
        } else {
            queuedTeleportRepository.queue(
                Teleport(
                    playerUUID = summonedPlayer.uniqueId,
                    targetPlayerUUID = destinationPlayer.uniqueId,
                    targetServerName = targetServer.name,
                    teleportType = TeleportType.TP,
                    createdAt = LocalDateTime.now()
                )
            )
            summonedPlayer.connect(targetServer, ServerConnectEvent.Reason.COMMAND)
        }
    }
}