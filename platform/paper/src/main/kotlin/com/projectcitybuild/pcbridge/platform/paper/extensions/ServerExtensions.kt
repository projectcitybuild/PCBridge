package com.projectcitybuild.pcbridge.platform.paper.extensions

import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID

fun Server.onlinePlayer(
    name: String,
    ignoreCase: Boolean = true,
): Player? = onlinePlayers.firstOrNull {
    it.name.equals(name, ignoreCase = ignoreCase)
}

fun Server.onlinePlayer(uuid: UUID): Player? = onlinePlayers.firstOrNull { it.uniqueId == uuid }

fun Server.onlinePlayersInWorld(world: World): List<Player> = onlinePlayers.filter { it.world.uid == world.uid }
