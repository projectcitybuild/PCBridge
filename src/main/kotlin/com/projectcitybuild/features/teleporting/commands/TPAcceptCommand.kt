package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.teleporting.PlayerTeleporter
import com.projectcitybuild.features.teleporting.repositories.TeleportRequestRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.Timer
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class TPAcceptCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val playerTeleporter: PlayerTeleporter,
    private val timer: Timer,
): BungeecordCommand {

    override val label: String = "tpaccept"
    override val permission = "pcbridge.tpa"
    override val usageHelp = "/tpaccept"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
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

        val targetPlayer = proxyServer.getPlayer(teleportRequest.targetUUID)

        input.player.send().info("Accepted teleport request")
        targetPlayer.send().info("${input.player.name} accepted your teleport request")

        when (teleportRequest.teleportType) {
            TeleportRequestRepository.TeleportType.TP_TO_PLAYER ->
                playerTeleporter.teleport(
                    player = targetPlayer,
                    destinationPlayer = input.player,
                    shouldCheckAllowingTP = false,
                )
            TeleportRequestRepository.TeleportType.SUMMON_PLAYER ->
                playerTeleporter.summon(
                    summonedPlayer = input.player,
                    destinationPlayer = targetPlayer,
                    shouldCheckAllowingTP = false,
                )
        }
    }
}
