package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.projectcitybuild.pcbridge.Permissions
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.command.CommandSender

class StaffChatCommand(
    private val server: Server,
) : SpigotCommand<StaffChatCommand.Args> {
    override val label = "a"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        val message =
            Component.text()
                .append(
                    Component.text("(Staff) ${sender.name}")
                        .color(NamedTextColor.YELLOW),
                )
                .append(
                    Component.text(" » ")
                        .color(NamedTextColor.GRAY),
                )
                .append(
                    Component.text(args.message),
                )
                .build()

        server.onlinePlayers
            .filter { it.hasPermission(Permissions.COMMAND_STAFF_CHAT) }
            .forEach { it.sendMessage(message) }
    }

    data class Args(
        val message: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(message = args.joinToString(separator = " "))
            }
        }
    }
}
