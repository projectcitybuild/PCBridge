package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.QueuedTeleport
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.modules.channels.NodeMessenger
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.repositories.QueuedPlayerTeleportRepository
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerTeleporter @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val queuedPlayerTeleportRepository: QueuedPlayerTeleportRepository,
    private val nodeMessenger: NodeMessenger,
) {
    enum class FailureReason {
        TARGET_PLAYER_DISALLOWS_TP,
    }

    fun teleport(
        player: ProxiedPlayer,
        destinationPlayer: ProxiedPlayer,
        shouldCheckAllowingTP: Boolean,
        shouldSupressTeleportedMessage: Boolean,
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
            nodeMessenger.sendToNode(
                nodeServer = destinationServer,
                subChannel = SubChannel.TP_SAME_SERVER,
                params = arrayOf(
                    player.uniqueId.toString(),
                    destinationPlayer.uniqueId.toString(),
                    false, // isSummon
                    shouldSupressTeleportedMessage,
                ),
            )
        } else {
            queuedPlayerTeleportRepository.queue(
                QueuedTeleport(
                    playerUUID = player.uniqueId,
                    targetPlayerUUID = destinationPlayer.uniqueId,
                    targetServerName = destinationServer.name,
                    teleportType = TeleportType.TP,
                    isSilentTeleport = shouldSupressTeleportedMessage,
                    createdAt = LocalDateTime.now()
                )
            )
            nodeMessenger.sendToNode(
                nodeServer = player.server.info,
                subChannel = SubChannel.TP_ACROSS_SERVER,
                params = arrayOf(
                    player.uniqueId.toString(),
                    destinationServer.name,
                ),
            )
        }

        return Success(Unit)
    }

    fun summon(
        summonedPlayer: ProxiedPlayer,
        destinationPlayer: ProxiedPlayer,
        shouldCheckAllowingTP: Boolean,
        shouldSupressTeleportedMessage: Boolean,
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
            nodeMessenger.sendToNode(
                nodeServer = targetServer,
                subChannel = SubChannel.TP_SAME_SERVER,
                params = arrayOf(
                    summonedPlayer.uniqueId.toString(),
                    destinationPlayer.uniqueId.toString(),
                    true,
                    shouldSupressTeleportedMessage,
                ),
            )
        } else {
            queuedPlayerTeleportRepository.queue(
                QueuedTeleport(
                    playerUUID = summonedPlayer.uniqueId,
                    targetPlayerUUID = destinationPlayer.uniqueId,
                    targetServerName = targetServer.name,
                    teleportType = TeleportType.TP,
                    isSilentTeleport = shouldSupressTeleportedMessage,
                    createdAt = LocalDateTime.now()
                )
            )
            nodeMessenger.sendToNode(
                nodeServer = summonedPlayer.server.info,
                subChannel = SubChannel.TP_ACROSS_SERVER,
                params = arrayOf(
                    summonedPlayer.uniqueId.toString(),
                    targetServer.name,
                )
            )
        }

        return Success(Unit)
    }
}