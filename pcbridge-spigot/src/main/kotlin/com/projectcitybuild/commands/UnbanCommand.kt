package com.projectcitybuild.commands

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.features.bans.usecases.UnbanUUID
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.command.CommandSender

class UnbanCommand(
    private val server: Server,
    private val unbanUUID: UnbanUUID,
) : SpigotCommand {

    override val label = "unban"
    override val permission = "pcbridge.ban.unban"
    override val usageHelp = "/unban <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsole) null else input.player

        val result = unbanUUID.unban(targetPlayerName, staffPlayer?.uniqueId)
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    UnbanUUID.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    UnbanUUID.FailureReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
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
