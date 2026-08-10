package com.projectcitybuild.pcbridge.paper.assembly.playerlookup

import com.projectcitybuild.pcbridge.core.playerlookup.OnlinePlayerFinder
import com.projectcitybuild.pcbridge.papersupport.support.spigot.extensions.onlinePlayer
import org.bukkit.Server
import java.util.UUID

class BukkitOnlinePlayerFinder(
    private val server: Server,
): OnlinePlayerFinder {
    override fun findByName(name: String): UUID?
        = server.onlinePlayer(name = name)?.uniqueId
}