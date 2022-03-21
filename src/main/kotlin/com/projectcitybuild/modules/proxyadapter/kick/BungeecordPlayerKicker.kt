package com.projectcitybuild.modules.proxyadapter.kick

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import javax.inject.Inject

class BungeecordPlayerKicker @Inject constructor(
    private val proxyServer: ProxyServer,
) : PlayerKicker {

    override fun kickByName(playerName: String, reason: String, context: PlayerKicker.KickContext) {
        val caseInsensitiveName = playerName.lowercase()

        proxyServer.players
            .firstOrNull { it.name.lowercase() == caseInsensitiveName }
            ?.disconnect(makeTextComponent(reason, context))
    }

    override fun kickByUUID(playerUUID: UUID, reason: String, context: PlayerKicker.KickContext) {
        proxyServer
            .getPlayer(playerUUID)
            ?.disconnect(makeTextComponent(reason, context))
    }

    override fun kickByIP(ip: String, reason: String, context: PlayerKicker.KickContext) {
        proxyServer.players
            .firstOrNull { it.socketAddress.toString() == ip }
            ?.disconnect(makeTextComponent(reason, context))
    }

    private fun makeTextComponent(message: String, context: PlayerKicker.KickContext): TextComponent {
        return when (context) {
            PlayerKicker.KickContext.FATAL ->
                TextComponent(message).apply {
                    color = ChatColor.RED
                }
        }
    }
}
