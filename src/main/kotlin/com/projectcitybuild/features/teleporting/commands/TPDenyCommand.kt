package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.teleporting.repositories.TeleportRequestRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.Timer
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class TPDenyCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val timer: Timer,
): BungeecordCommand {

    override val label: String = "tpdeny"
    override val permission = "pcbridge.tpa"
    override val usageHelp = "/tpdeny"

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

        input.player.send().info("Declined teleport request")
        targetPlayer.send().info("${input.player.name} declined your teleport request")
    }
}
