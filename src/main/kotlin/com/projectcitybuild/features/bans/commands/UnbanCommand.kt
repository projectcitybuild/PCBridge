package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.bans.usecases.UnbanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class UnbanCommand @Inject constructor(
    private val server: Server,
    private val unbanUseCase: UnbanUseCase,
) : SpigotCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"
    override val usageHelp = "/unban <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsole) null else input.player

        val result = unbanUseCase.unban(targetPlayerName, staffPlayer?.uniqueId)
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    UnbanUseCase.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    UnbanUseCase.FailureReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
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
