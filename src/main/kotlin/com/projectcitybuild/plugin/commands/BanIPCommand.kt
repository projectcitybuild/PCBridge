package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.BanIP
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class BanIPCommand @Inject constructor(
    private val server: Server,
    private val banIP: BanIP,
) : SpigotCommand {

    override val label = "banip"
    override val permission = "pcbridge.ban.banip"
    override val usageHelp = "/banip <name|ip> <reason>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isEmpty() || input.args.size < 2) {
            throw InvalidCommandArgumentsException()
        }

        val targetIP = server.onlinePlayers
            .firstOrNull { it.name.lowercase() == input.args.first().lowercase() }
            ?.address?.toString()
            ?: input.args.first()

        val result = banIP.execute(
            ip = targetIP,
            bannerUUID = input.player.uniqueId,
            bannerName = input.sender.name,
            reason = input.args.joinWithWhitespaces(1 until input.args.size) ?: "No reason given",
        )
        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    BanIP.FailureReason.IP_ALREADY_BANNED -> "$targetIP is already banned"
                    BanIP.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
            is Success -> input.sender.send().success("IP $targetIP has been banned")
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
