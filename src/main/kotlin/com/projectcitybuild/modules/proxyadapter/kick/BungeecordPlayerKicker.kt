package com.projectcitybuild.modules.proxyadapter.kick

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import javax.inject.Inject

class BungeecordPlayerKicker @Inject constructor(
    private val proxyServer: ProxyServer,
): PlayerKicker {

    override fun kick(playerName: String, reason: TextComponent) {
        val caseInsensitiveName = playerName.lowercase()

        proxyServer.players
            .firstOrNull { it.name.lowercase() == caseInsensitiveName }
            ?.disconnect(reason)
    }

    override fun kick(playerUUID: UUID, reason: TextComponent) {
        proxyServer.getPlayer(playerUUID).disconnect(reason)
    }
}