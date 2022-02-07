package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.QueuedTeleport
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerTeleporter @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val queuedTeleportRepository: QueuedTeleportRepository,
) {
    enum class FailureReason {
        TARGET_PLAYER_DISALLOWS_TP,
    }

    fun teleport(
        player: ProxiedPlayer,
        destinationPlayer: ProxiedPlayer,
        shouldCheckAllowingTP: Boolean
    ): Result<Unit, FailureReason> {
        if (shouldCheckAllowingTP) {
            val targetPlayerConfig = playerConfigRepository.get(destinationPlayer.uniqueId)!!
            if (!targetPlayerConfig.isAllowingTPs) {
                return Failure(FailureReason.TARGET_PLAYER_DISALLOWS_TP)
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

        return Success(Unit)
    }

    fun summon(
        summonedPlayer: ProxiedPlayer,
        destinationPlayer: ProxiedPlayer,
        shouldCheckAllowingTP: Boolean
    ): Result<Unit, FailureReason> {
        if (shouldCheckAllowingTP) {
            val summonedPlayerConfig = playerConfigRepository.get(summonedPlayer.uniqueId)!!
            if (!summonedPlayerConfig.isAllowingTPs) {
                return Failure(FailureReason.TARGET_PLAYER_DISALLOWS_TP)
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

        return Success(Unit)
    }
}