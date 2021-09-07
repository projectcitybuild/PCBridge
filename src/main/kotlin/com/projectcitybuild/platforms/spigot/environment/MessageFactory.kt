package com.projectcitybuild.platforms.spigot.environment

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageFactory(
    private val player: Player
) {
    fun error(message: String) {
        player.spigot().sendMessage(TextComponent().also {
            it.addExtra(
                TextComponent("Error").also {
                    it.color = ChatColor.RED
                }
            )
            it.addExtra(
                TextComponent(" » ").also {
                    it.color = ChatColor.GOLD
                }
            )
            it.addExtra(
                TextComponent("$message").also {
                    it.color = ChatColor.WHITE
                }
            )
        })
    }

    fun success(message: String) {
        player.spigot().sendMessage(TextComponent().also {
            it.addExtra(
                TextComponent("Success").also {
                    it.color = ChatColor.GREEN
                }
            )
            it.addExtra(
                TextComponent(" » ").also {
                    it.color = ChatColor.GOLD
                }
            )
            it.addExtra(
                TextComponent("$message").also {
                    it.color = ChatColor.WHITE
                }
            )
        })
    }
}

fun Player.send(): MessageFactory {
    return MessageFactory(this)
}

fun CommandSender.send(): MessageFactory {
    return MessageFactory(this as Player)
}