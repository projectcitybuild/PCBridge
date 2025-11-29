package com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.UUID

fun Server.broadcastRich(message: String)
    = broadcast(MiniMessage.miniMessage().deserialize(message))

fun Server.onlinePlayer(name: String, ignoreCase: Boolean = true): Player?
    = onlinePlayers.firstOrNull { it.name.equals(name, ignoreCase = ignoreCase) }

fun Server.onlinePlayer(uuid: UUID): Player?
    = onlinePlayers.firstOrNull { it.uniqueId == uuid }
