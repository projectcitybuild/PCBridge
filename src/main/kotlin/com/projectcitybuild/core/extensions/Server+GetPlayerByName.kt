package com.projectcitybuild.core.extensions

import org.bukkit.Server
import org.bukkit.entity.Player

fun Server.getOnlinePlayer(name: String) : Player? {
    return onlinePlayers?.
            filter { player -> player.name.toLowerCase() == name.toLowerCase() }?.
            first()
}