package com.projectcitybuild.modules.proxyadapter.broadcast

import com.projectcitybuild.modules.proxyadapter.messages.TextComponentBox
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class BungeecordMessageBroadcaster @Inject constructor(
    private val proxyServer: ProxyServer,
) : MessageBroadcaster {

    override fun broadcastToAll(message: TextComponentBox) {
        proxyServer.broadcast(message.textComponent)
    }
}
