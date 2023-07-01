package com.projectcitybuild.commands

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.features.chat.usecases.MutePlayer
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.command.CommandSender

class MuteCommand(
    private val server: Server,
    private val mute: MutePlayer,
) : SpigotCommand {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"
    override val usageHelp = "/mute <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()

        val result = mute.execute(
            willBeMuted = true,
            targetPlayerName = targetPlayerName,
            onlinePlayers = server.onlinePlayers.toList(),
        )
        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    MutePlayer.FailureReason.PLAYER_NOT_ONLINE -> "$targetPlayerName is not online"
                }
            )
            is Success -> {
                input.sender.send().success("${result.value.name} has been muted")
                result.value.send().info("You have been muted by ${input.sender.name}")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}
