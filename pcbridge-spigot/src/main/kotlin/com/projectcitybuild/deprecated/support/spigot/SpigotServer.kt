package com.projectcitybuild.support.spigot

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.UUID

class SpigotServer(
    private val server: Server,
) {
    enum class KickContext {
        FATAL,
    }

    fun kickByName(playerName: String, reason: String, context: KickContext) {
        val caseInsensitiveName = playerName.lowercase()

        server.onlinePlayers
            .firstOrNull { it.name.lowercase() == caseInsensitiveName }
            ?.kickPlayer(makeKickMessage(reason, context))
    }

    fun kickByUUID(playerUUID: UUID, reason: String, context: KickContext) {
        server.getPlayer(playerUUID)
            ?.kickPlayer(makeKickMessage(reason, context))
    }

    fun kickByIP(ip: String, reason: String, context: KickContext) {
        server.onlinePlayers
            .firstOrNull { it.address.toString() == ip }
            ?.kickPlayer(makeKickMessage(reason, context))
    }

    private fun makeKickMessage(message: String, context: KickContext): String {
        return when (context) {
            KickContext.FATAL ->
                TextComponent(message).apply {
                    color = ChatColor.RED
                }
        }.toLegacyText()
    }

    fun broadcastMessage(message: TextComponent) {
        server.broadcastMessage(message.toLegacyText())
    }

    fun createInventory(owner: Player, size: Int, title: String): Inventory {
        return server.createInventory(owner, size, title)
    }
}