package com.projectcitybuild.modules.sessioncache

import java.util.*

class BungeecordSessionCache {
    val lastWhispered = HashMap<UUID, UUID>()
    val afkPlayerList: MutableList<UUID> = mutableListOf()
}
