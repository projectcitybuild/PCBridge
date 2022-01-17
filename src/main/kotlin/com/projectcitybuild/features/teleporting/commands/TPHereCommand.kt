package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Teleport
import com.projectcitybuild.features.teleporting.repositories.QueuedTeleportRepository
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ServerConnectEvent
import java.time.LocalDateTime
import javax.inject.Inject

class TPHereCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val queuedTeleportRepository: QueuedTeleportRepository,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label: String = "tphere"
    override val permission = "pcbridge.tp.here"
    override val usageHelp = "/tphere <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)!!
        if (!targetPlayerConfig.isAllowingTPs) {
            input.sender.send().error("$targetPlayerName is disallowing teleports")
            return
        }

        val targetServer = targetPlayer.server.info
        val isTargetPlayerOnSameServer = input.player.server.info.name == targetPlayer.server.info.name
        if (isTargetPlayerOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.TP_IMMEDIATELY,
                arrayOf(
                    targetPlayer.uniqueId.toString(),
                    input.player.uniqueId.toString(),
                )
            ).send()
        } else {
            val teleport = Teleport(
                playerUUID = input.player.uniqueId,
                targetPlayerUUID = targetPlayer.uniqueId,
                targetServer.name,
                createdAt = LocalDateTime.now()
            )
            queuedTeleportRepository.queue(teleport)
            input.player.connect(targetServer, ServerConnectEvent.Reason.COMMAND)
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
