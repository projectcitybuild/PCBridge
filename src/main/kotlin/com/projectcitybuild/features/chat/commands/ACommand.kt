package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import javax.inject.Inject

class ACommand @Inject constructor(
    private val server: Server
) : SpigotCommand {

    override val label: String = "a"
    override val permission = "pcbridge.chat.staff_channel"
    override val usageHelp = "/a <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val message = input.args.joinToString(separator = " ")
        val senderName = input.player?.displayName ?: "CONSOLE"

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
