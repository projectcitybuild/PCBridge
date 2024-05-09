package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.features.bans.actions.BanIP
import com.projectcitybuild.utils.Failure
import com.projectcitybuild.utils.Success
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BanIPCommand(
    private val server: Server,
    private val banIP: BanIP,
): SpigotCommand<BanIPCommand.Args> {
    override val label = "banip"

    override val usage = CommandHelpBuilder()

    override suspend fun run(sender: CommandSender, args: Args) {
        val targetIP = server.onlinePlayers
            .firstOrNull { it.name.lowercase() == args.target.lowercase() }
            ?.address?.toString()
            ?: args.target

        val result = banIP.execute(
            ip = targetIP,
            bannerUUID = if (sender is Player) sender.uniqueId else null,
            bannerName = sender.name,
            reason = args.reason,
        )
        val message = when (result) {
            is Failure -> when (result.reason) {
                BanIP.FailureReason.IP_ALREADY_BANNED -> Component.text("$targetIP is already banned")
                    .color(NamedTextColor.RED)
                BanIP.FailureReason.INVALID_IP -> Component.text("$targetIP is not a valid IP")
                    .color(NamedTextColor.RED)
            }
            is Success -> Component.text("IP $targetIP has been banned")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
        }
        sender.sendMessage(message)
    }

    data class Args(
        val target: String,
        val reason: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(
                    target = args[0],
                    reason = if (args.size > 1) args.drop(1).joinToString(separator = " ") else ""
                )
            }
        }
    }
}
