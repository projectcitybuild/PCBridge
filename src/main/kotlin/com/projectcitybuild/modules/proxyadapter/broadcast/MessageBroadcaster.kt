package com.projectcitybuild.modules.proxyadapter.broadcast

import net.md_5.bungee.api.chat.TextComponent

interface MessageBroadcaster {
    fun broadcastToAll(message: TextComponent)
}