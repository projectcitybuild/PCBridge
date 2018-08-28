package com.projectcitybuild.spigot.extensions

import org.bukkit.Server
import org.bukkit.entity.Player

fun Server.getOnlinePlayer(name: String) : Player? {
    return onlinePlayers?.find { player -> player.name.toLowerCase() == name.toLowerCase() }
}