package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.features.teleporting.PlayerTeleporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.PlatformTimer
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.TeleportRequestRepository
import org.bukkit.Server
import javax.inject.Inject

class TPAcceptCommand @Inject constructor(
    private val server: Server,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val playerTeleporter: PlayerTeleporter,
    private val timer: PlatformTimer,
    private val logger: PlatformLogger,
) : SpigotCommand {

    override val label = "tpaccept"
    override val permission = "pcbridge.tpa"
    override val usageHelp = "/tpaccept"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val teleportRequest = teleportRequestRepository.get(input.player.uniqueId)
        if (teleportRequest == null) {
            input.sender.send().error("No pending teleport request")
            return
        }

        timer.cancel(teleportRequest.timerIdentifier)
        teleportRequestRepository.delete(input.player.uniqueId)

        logger.debug(teleportRequest.toString())

        val requesterPlayer = server.getPlayer(teleportRequest.requesterUUID)
        if (requesterPlayer == null) {
            input.player.send().error("Player not found")
            return
        }

        val targetPlayer = server.getPlayer(teleportRequest.targetUUID)
        if (targetPlayer == null) {
            input.player.send().error("Player not found")
            return
        }

        when (teleportRequest.teleportType) {
            TeleportRequestRepository.TeleportType.TP_TO_PLAYER -> {
                playerTeleporter.teleport(
                    player = requesterPlayer,
                    destinationPlayer = targetPlayer,
                    shouldCheckAllowingTP = false,
                    shouldSupressTeleportedMessage = false,
                )
                targetPlayer.send().info("Accepted teleport request")
                requesterPlayer.send().info("${input.player.name} accepted your teleport request")
            }
            TeleportRequestRepository.TeleportType.SUMMON_PLAYER -> {
                playerTeleporter.summon(
                    summonedPlayer = targetPlayer,
                    destinationPlayer = requesterPlayer,
                    shouldCheckAllowingTP = false,
                    shouldSupressTeleportedMessage = false,
                )
                targetPlayer.send().info("Accepted summon request")
                requesterPlayer.send().info("${input.player.name} accepted your summon request")
            }
        }
    }
}
