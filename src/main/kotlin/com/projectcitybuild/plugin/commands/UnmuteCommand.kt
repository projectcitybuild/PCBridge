package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.chat.usecases.MuteUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.support.annotations.annotations.Command
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

@Command(
    name = "unbanip",
    desc = "Allows a muted player to talk in chat again",
    usage = "/unmute <name>",
)
class UnmuteCommand @Inject constructor(
    private val server: Server,
    private val mute: MuteUseCase,
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
                    MuteUseCase.FailureReason.PLAYER_NOT_ONLINE -> "$targetPlayerName is not online"
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
