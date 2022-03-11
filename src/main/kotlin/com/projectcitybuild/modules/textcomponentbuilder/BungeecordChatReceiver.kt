package com.projectcitybuild.modules.textcomponentbuilder

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent

@Deprecated("Use PlatformMessageSender instead")
private class BungeecordChatReceiver(private val player: CommandSender): ChatMessageReceiver {
    override fun sendMessage(component: BaseComponent) {
        player.sendMessage(component)
    }
}

@Deprecated("Use PlatformMessageSender instead")
fun CommandSender.send(): MessageSender {
    return MessageSender(BungeecordChatReceiver(this))
}