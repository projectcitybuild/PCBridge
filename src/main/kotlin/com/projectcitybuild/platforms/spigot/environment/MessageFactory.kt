package com.projectcitybuild.platforms.spigot.environment

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

class MessageFactory(
    private val player: Player
) {
    fun error(message: String) {
        player.spigot().sendMessage(TextComponent().also {
            it.addExtra(
                TextComponent("ERROR » ").apply {
                    it.color = ChatColor.RED
                    it.isBold = true
                }
            )
            it.addExtra(
                TextComponent(" $message").apply {
                    it.color = ChatColor.RED
                }
            )
        })
    }

    fun success(message: String) {
        player.spigot().sendMessage(TextComponent().also {
            it.addExtra(
                TextComponent("SUCCESS » ").apply {
                    it.color = ChatColor.GREEN
                }
            )
            it.addExtra(
                TextComponent(" $message").apply {
                    it.color = ChatColor.GREEN
                }
            )
        })
    }
}

fun Player.send(): MessageFactory {
    return MessageFactory(this)
}