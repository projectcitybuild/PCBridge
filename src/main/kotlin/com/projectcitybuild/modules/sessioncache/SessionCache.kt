package com.projectcitybuild.modules.sessioncache

import org.bukkit.Location
import java.util.*

class SessionCache {
    val afkPlayerList: MutableList<UUID> = mutableListOf()
    val pendingWarps = HashMap<UUID, Location>()
}