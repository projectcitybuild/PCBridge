package com.projectcitybuild.old_modules.playerconfig

import com.projectcitybuild.entities.LegacyPlayerConfig
import java.util.*

/**
 * A memory cache of configs for players currently online
 * on any of the connected servers
 */
class LegacyPlayerConfigCache {
    private val cache = HashMap<UUID, LegacyPlayerConfig>()

    fun get(uuid: UUID): LegacyPlayerConfig? {
       return cache[uuid]
    }

    fun put(uuid: UUID, player: LegacyPlayerConfig) {
        cache[uuid] = player
    }

    fun flush() {
        cache.clear()
    }
}