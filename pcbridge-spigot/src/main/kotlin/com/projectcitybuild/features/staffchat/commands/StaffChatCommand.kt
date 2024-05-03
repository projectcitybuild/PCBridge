package com.projectcitybuild.features.staffchat.commands

import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.command.CommandSender

class StaffChatCommand(
    private val server: Server,
): SpigotCommand<StaffChatCommand.Args> {
    override val label = "a"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        val message = Component.text()
            .append(
                Component.text("(Staff) ${sender.name}")
                    .color(NamedTextColor.YELLOW)
            )
            .append(
                Component.text(" » ")
                    .color(NamedTextColor.GRAY)
            )
            .append(
                Component.text(args.message)
            )
            .build()

        server.onlinePlayers
            .filter { it.hasPermission("pcbridge.chat.staff_channel") }
            .forEach { it.sendMessage(message) }
    }

    data class Args(
        val message: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun tryParse(args: List<String>): Args? {
                if (args.isEmpty()) {
                    return null
                }
                return Args(message = args.joinToString(separator = " "))
            }
        }
    }
}