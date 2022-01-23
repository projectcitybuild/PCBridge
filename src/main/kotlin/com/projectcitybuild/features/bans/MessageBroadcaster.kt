package com.projectcitybuild.features.bans

import net.md_5.bungee.api.chat.TextComponent

interface MessageBroadcaster {
    fun broadcastToAll(message: TextComponent)
}