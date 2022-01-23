package com.projectcitybuild.modules.proxyadapter.playerlist

import net.md_5.bungee.api.ProxyServer
import java.util.*
import javax.inject.Inject

class BungeecordOnlinePlayerList @Inject constructor(
    private val proxyServer: ProxyServer,
): OnlinePlayerList {

    override fun getUUID(name: String): UUID? {
        val caseInsensitiveName = name.lowercase()

        return proxyServer.players
            .firstOrNull { it.name.lowercase() == caseInsensitiveName }
            ?.uniqueId
    }
}