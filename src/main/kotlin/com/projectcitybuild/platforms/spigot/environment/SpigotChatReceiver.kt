package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.core.contracts.ChatMessageReceiver
import com.projectcitybuild.modules.players.MessageSender
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private class SpigotChatReceiver(private val player: Player): ChatMessageReceiver {

    override fun sendMessage(component: BaseComponent) {
        player.spigot().sendMessage(component)
    }
}

fun Player.send(): MessageSender {

    return MessageSender(SpigotChatReceiver(this))
}

fun CommandSender.send(): MessageSender {

    return MessageSender(SpigotChatReceiver(this as Player))
}