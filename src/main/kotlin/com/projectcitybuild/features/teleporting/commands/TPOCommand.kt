package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.teleporting.PlayerTeleporter
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class TPOCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerTeleporter: PlayerTeleporter,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label: String = "tpo"
    override val permission = "pcbridge.tpo.use"
    override val usageHelp = "/tpo <name> [--silent]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty() || input.args.size > 2) {
            throw InvalidCommandArgumentsException()
        }

        var isSilentTP = false
        if (input.args.size == 2) {
            if (input.args[1] == "--silent") {
                isSilentTP = true
            } else {
                input.player.send().error("${input.args[1]} is not a valid argument")
                return
            }
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot teleport to yourself")
            return
        }

        playerTeleporter.teleport(
            player = input.player,
            destinationPlayer = targetPlayer,
            shouldCheckAllowingTP = false,
            shouldSupressTeleportedMessage = isSilentTP,
        )
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players
                .map { it.name }
                .filter { it != sender?.name }

            args.size == 1 -> proxyServer.players
                .map { it.name }
                .filter { it != sender?.name }
                .filter { it.lowercase().startsWith(args.first().lowercase()) }

            args.size == 2 -> listOf("--silent")

            else -> null
        }
    }
}
