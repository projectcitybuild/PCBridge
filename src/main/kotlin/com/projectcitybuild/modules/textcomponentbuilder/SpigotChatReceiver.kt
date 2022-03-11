package com.projectcitybuild.modules.textcomponentbuilder

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Deprecated("Use PlatformMessageSender instead")
private class SpigotChatReceiver(private val player: Player): ChatMessageReceiver {

    override fun sendMessage(component: BaseComponent) {
        player.spigot().sendMessage(component)
    }
}

@Deprecated("Use PlatformMessageSender instead")
fun Player.send(): MessageSender {

    return MessageSender(SpigotChatReceiver(this))
}

@Deprecated("Use PlatformMessageSender instead")
fun CommandSender.send(): MessageSender {

    return MessageSender(SpigotChatReceiver(this as Player))
}