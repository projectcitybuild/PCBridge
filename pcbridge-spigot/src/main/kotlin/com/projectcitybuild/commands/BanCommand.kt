package com.projectcitybuild.commands

import com.projectcitybuild.extensions.joinWithWhitespaces
import com.projectcitybuild.features.bans.usecases.BanUUID
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.command.CommandSender

class BanCommand(
    private val server: Server,
    private val banUUID: BanUUID,
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

        val result = banUUID.ban(
            targetPlayerName,
            bannerUUID = staffPlayer?.uniqueId,
            bannerName = input.sender.name ?: "CONSOLE",
            reason
        )
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    BanUUID.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    BanUUID.FailureReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
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
