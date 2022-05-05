package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.teleporting.PlayerTeleporter
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class TPCommand @Inject constructor(
    private val server: Server,
    private val playerTeleporter: PlayerTeleporter,
    private val nameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "tp"
    override val permission = "pcbridge.tp.use"
    override val usageHelp = "/tp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot teleport to yourself")
            return
        }

        val result = playerTeleporter.teleport(
            player = input.player,
            destinationPlayer = targetPlayer,
            shouldCheckAllowingTP = true,
            shouldSupressTeleportedMessage = false,
        )
        if (result is Failure) {
            when (result.reason) {
                PlayerTeleporter.FailureReason.TARGET_PLAYER_DISALLOWS_TP ->
                    input.player.send().error("${targetPlayer.name} is disallowing teleports")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() ->
                server.onlinePlayers
                    .map { it.name }
                    .filter { it != sender?.name }

            args.size == 1 ->
                server.onlinePlayers
                    .map { it.name }
                    .filter { it != sender?.name }
                    .filter { it.lowercase().startsWith(args.first().lowercase()) }

            else -> null
        }
    }
}
