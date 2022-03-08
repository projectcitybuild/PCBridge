package com.projectcitybuild.modules.messaging.senders

import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.messaging.MessageBuilder
import com.projectcitybuild.modules.messaging.serializers.TextComponentSerializer
import net.md_5.bungee.api.ProxyServer
import java.util.*
import javax.inject.Inject

class BungeecordMessageSender @Inject constructor(
    private val proxyServer: ProxyServer,
    private val logger: PlatformLogger,
): PlatformMessageSender {

    override fun process(
        playerUUID: UUID,
        builder: MessageBuilder
    ) {
        val player = proxyServer.getPlayer(playerUUID)
        if (player == null) {
            logger.warning("Attempted to send message to offline player ($playerUUID)")
            return
        }
        TextComponentSerializer().serialize(builder).let {
            player.sendMessage(it)
        }
    }
}