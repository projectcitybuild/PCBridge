package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.chat.usecases.ReplyUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import javax.inject.Inject

class ReplyCommand @Inject constructor(
    private val server: Server,
    private val reply: ReplyUseCase,
) : SpigotCommand {

    override val label = "reply"
    override val aliases = arrayOf("r")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/reply <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val result = reply.execute(
            player = input.player,
            onlinePlayers = server.onlinePlayers.toList(),
            message = input.args.joinToString(separator = " "),
        )
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    is ReplyUseCase.FailureReason.NO_ONE_TO_REPLY_TO -> "You have not been direct messaged by anyone yet"
                    is ReplyUseCase.FailureReason.PLAYER_NOT_ONLINE -> "Player not online"
                    is ReplyUseCase.FailureReason.IGNORED -> "Cannot send. You are being ignored by ${result.reason.targetPlayerName}"
                }
            )
        }
    }
}
