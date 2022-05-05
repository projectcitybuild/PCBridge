package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.chat.usecases.WhisperUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class WhisperCommand @Inject constructor(
    private val server: Server,
    private val whisper: WhisperUseCase,
) : SpigotCommand {

    override val label = "whisper"
    override val aliases = arrayOf("msg", "m", "w", "t", "tell", "pm")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/whisper <name> <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val message = input.args.joinWithWhitespaces(1 until input.args.size)
            ?: throw InvalidCommandArgumentsException()

        val result = whisper.execute(
            whisperingPlayer = input.player,
            targetPlayerName = targetPlayerName,
            onlinePlayers = server.onlinePlayers.toList(),
            message = message,
        )
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    WhisperUseCase.FailureReason.PLAYER_NOT_ONLINE -> "Player not online"
                    WhisperUseCase.FailureReason.CANNOT_WHISPER_SELF -> "You cannot directly message yourself"
                    WhisperUseCase.FailureReason.BEING_IGNORED -> "Cannot send. You are being ignored by that player"
                }
            )
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
