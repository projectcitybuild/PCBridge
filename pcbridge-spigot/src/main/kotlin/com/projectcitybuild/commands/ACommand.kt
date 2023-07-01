package com.projectcitybuild.commands

import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server

class ACommand(
    private val server: Server
) : SpigotCommand {

    override val label = "a"
    override val permission = "pcbridge.chat.staff_channel"
    override val usageHelp = "/a <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val message = input.args.joinToString(separator = " ")
        val senderName = if (input.isConsole) "CONSOLE" else input.player.displayName

        server.onlinePlayers.forEach { player ->
            if (player.hasPermission("pcbridge.chat.staff_channel"))
                player.spigot().sendMessage(
                    TextComponent()
                        .add("(Staff) $senderName") { it.color = ChatColor.YELLOW }
                        .add(" » ") { it.color = ChatColor.GRAY }
                        .add(TextComponent.fromLegacyText(message))
                )
        }
    }
}
