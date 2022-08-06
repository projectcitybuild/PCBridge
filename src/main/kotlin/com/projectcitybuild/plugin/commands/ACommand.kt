package com.projectcitybuild.plugin.commands

import com.projectcitybuild.modules.textcomponentbuilder.add
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.support.annotations.annotations.Command
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import javax.inject.Inject

@Command(
    name = "a",
    desc = "Sends a message to all staff currently online",
    usage = "/a <message>",
)
class ACommand @Inject constructor(
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
