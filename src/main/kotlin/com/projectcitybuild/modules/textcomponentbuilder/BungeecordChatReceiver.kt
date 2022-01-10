package com.projectcitybuild.modules.textcomponentbuilder

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent

private class BungeecordChatReceiver(private val player: CommandSender): ChatMessageReceiver {
    override fun sendMessage(component: BaseComponent) {
        player.sendMessage(component)
    }
}

fun CommandSender.send(): MessageSender {
    return MessageSender(BungeecordChatReceiver(this))
}