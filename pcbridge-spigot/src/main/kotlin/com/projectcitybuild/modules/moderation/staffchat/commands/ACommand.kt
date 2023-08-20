package com.projectcitybuild.modules.moderation.staffchat.commands

import com.projectcitybuild.Permissions
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player

class ACommand(
    private val server: Server
) {
    fun execute(commandSender: Player, message: String) {
        server.onlinePlayers
            .filter { it.hasPermission(Permissions.COMMAND_STAFF_CHAT) }
            .forEach { player ->
                player.spigot().sendMessage(
                    TextComponent()
                        .add("(Staff) ${commandSender.displayName}") { it.color = ChatColor.YELLOW }
                        .add(" » ") { it.color = ChatColor.GRAY }
                        .add(TextComponent.fromLegacyText(message))
                )
            }
    }
}
