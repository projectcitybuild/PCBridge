package com.projectcitybuild.support.spigot.kick

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import java.util.UUID
import javax.inject.Inject

class SpigotPlayerKicker @Inject constructor(
    private val server: Server,
) : PlayerKicker {

    override fun kickByName(playerName: String, reason: String, context: PlayerKicker.KickContext) {
        val caseInsensitiveName = playerName.lowercase()

        server.onlinePlayers
            .firstOrNull { it.name.lowercase() == caseInsensitiveName }
            ?.kickPlayer(makeMessage(reason, context))
    }

    override fun kickByUUID(playerUUID: UUID, reason: String, context: PlayerKicker.KickContext) {
        server.getPlayer(playerUUID)
            ?.kickPlayer(makeMessage(reason, context))
    }

    override fun kickByIP(ip: String, reason: String, context: PlayerKicker.KickContext) {
        server.onlinePlayers
            .firstOrNull { it.address.toString() == ip }
            ?.kickPlayer(makeMessage(reason, context))
    }

    private fun makeMessage(message: String, context: PlayerKicker.KickContext): String {
        return when (context) {
            PlayerKicker.KickContext.FATAL ->
                TextComponent(message).apply {
                    color = ChatColor.RED
                }
        }.toLegacyText()
    }
}
