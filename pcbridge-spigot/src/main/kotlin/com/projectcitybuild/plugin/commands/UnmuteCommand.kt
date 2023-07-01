package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.chat.usecases.MutePlayer
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.command.CommandSender

class UnmuteCommand(
    private val server: Server,
    private val mute: MutePlayer,
) : SpigotCommand {

    override val label = "unmute"
    override val permission = "pcbridge.chat.unmute"
    override val usageHelp = "/unmute <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()

        val result = mute.execute(
            willBeMuted = false,
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
                input.sender.send().success("${result.value.name} has been unmuted")
                result.value.send().info("You have been unmuted by ${input.sender.name}")
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
