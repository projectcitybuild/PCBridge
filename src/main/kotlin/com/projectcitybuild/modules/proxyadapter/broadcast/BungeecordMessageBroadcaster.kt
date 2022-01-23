package com.projectcitybuild.modules.proxyadapter.broadcast

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import javax.inject.Inject

class BungeecordMessageBroadcaster @Inject constructor(
    private val proxyServer: ProxyServer,
): MessageBroadcaster {

    override fun broadcastToAll(message: TextComponent) {
        proxyServer.broadcast(message)
    }
}