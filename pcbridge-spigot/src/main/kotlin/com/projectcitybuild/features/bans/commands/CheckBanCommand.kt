package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.features.bans.actions.CheckUUIDBan
import com.projectcitybuild.utils.Failure
import com.projectcitybuild.utils.Success
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender

class CheckBanCommand(
    private val checkUUIDBan: CheckUUIDBan,
): SpigotCommand<CheckBanCommand.Args> {
    override val label = "checkban"

    override val usage = CommandHelpBuilder()

    override suspend fun run(sender: CommandSender, args: Args) {
        val result = checkUUIDBan.getBan(args.targetPlayerName)
        val message = when (result) {
            is Failure -> when (result.reason) {
                CheckUUIDBan.FailureReason.PLAYER_DOES_NOT_EXIST -> Component
                    .text("Could not find UUID for ${args.targetPlayerName}. This player likely doesn't exist")
                    .color(NamedTextColor.RED)
            }
            is Success -> result.value.let { ban ->
                if (ban == null) {
                    Component.text("${args.targetPlayerName} is not currently banned")
                        .color(NamedTextColor.RED)
                } else {
                    Component.text("""
                            ${ChatColor.RED}${args.targetPlayerName} is currently banned.
                            ${ChatColor.GRAY}---
                            ${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            ${ChatColor.GRAY}Date » ${ChatColor.WHITE}${ban.dateOfBan}
                            ${ChatColor.GRAY}Expires » ${ChatColor.WHITE}${ban.expiryDate}
                        """.trimIndent())
                }
            }
        }
        sender.sendMessage(message)
    }

    data class Args(
        val targetPlayerName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(targetPlayerName = args[0])
            }
        }
    }
}
