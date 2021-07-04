package com.projectcitybuild.platforms.bungeecord.extensions

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

fun ProxyServer.playerByNameIgnoringCase(name: String): ProxiedPlayer? {
    return players.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }
}