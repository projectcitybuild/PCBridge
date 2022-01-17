package com.projectcitybuild.modules.sessioncache

import dagger.Reusable
import java.util.*
import javax.inject.Inject

@Reusable
class BungeecordSessionCache @Inject constructor() {
    val lastWhispered = HashMap<UUID, UUID>()
    val afkPlayerList: MutableList<UUID> = mutableListOf()

    fun flush() {
        lastWhispered.clear()
        afkPlayerList.clear()
    }
}
