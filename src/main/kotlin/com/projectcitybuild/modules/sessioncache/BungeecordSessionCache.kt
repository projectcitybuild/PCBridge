package com.projectcitybuild.modules.sessioncache

import dagger.Reusable
import java.util.*

@Reusable
class BungeecordSessionCache {
    val lastWhispered = HashMap<UUID, UUID>()
    val afkPlayerList: MutableList<UUID> = mutableListOf()
}
