package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.features.bans.actions.UnbanIP
import com.projectcitybuild.utils.Failure
import com.projectcitybuild.utils.Success
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnbanIPCommand(
    private val unbanIP: UnbanIP,
): SpigotCommand<UnbanIPCommand.Args> {
    override val label = "unbanip"

    override val usage = CommandHelpBuilder()

    override suspend fun run(sender: CommandSender, args: Args) {
        val result = unbanIP.execute(
            ip = args.ip,
            unbannerUUID = if (sender is Player) sender.uniqueId else null,
            unbannerName = sender.name,
        )

        val message = when (result) {
            is Failure -> Component.text(result.reason.toMessage(args.ip))
                .color(NamedTextColor.RED)
            is Success -> Component.text("IP ${args.ip} has been unbanned")
                .color(NamedTextColor.GRAY)
        }
        sender.sendMessage(message)
    }

    data class Args(
        val ip: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(ip = args[0])
            }
        }
    }
}

private fun UnbanIP.FailureReason.toMessage(targetIP: String) = when (this) {
    UnbanIP.FailureReason.IP_NOT_BANNED -> "$targetIP is not currently banned"
    UnbanIP.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
}
