package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.bans.usecases.BanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class BanCommand @Inject constructor(
    private val server: Server,
    private val banUseCase: BanUseCase,
) : SpigotCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"
    override val usageHelp = "/ban <name> [reason]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val staffPlayer = if (input.isConsole) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        val result = banUseCase.ban(
            targetPlayerName,
            bannerUUID = staffPlayer?.uniqueId,
            bannerName = input.sender.name ?: "CONSOLE",
            reason
        )
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    BanUseCase.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    BanUseCase.FailureReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
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
