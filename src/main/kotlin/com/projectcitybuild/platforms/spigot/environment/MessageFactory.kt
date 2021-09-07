package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.platforms.spigot.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageFactory(
    private val player: Player
) {
    fun error(message: String) {
        player.spigot().sendMessage(
            TextComponent()
                .add("Error") { it.color = ChatColor.RED }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun success(message: String) {
        player.spigot().sendMessage(
            TextComponent()
                .add("Success") { it.color = ChatColor.GREEN }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun info(message: String) {
        player.spigot().sendMessage(
            TextComponent()
                .add("Info") { it.color = ChatColor.GRAY }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }
}

fun Player.send(): MessageFactory {
    return MessageFactory(this)
}

fun CommandSender.send(): MessageFactory {
    return MessageFactory(this as Player)
}