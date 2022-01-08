package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import java.util.*

/**
 * A memory cache of configs for players currently online
 * on any of the connected servers
 */
class PlayerConfigCache {
    private val cache = HashMap<UUID, PlayerConfig>()

    fun get(uuid: UUID): PlayerConfig? {
       return cache[uuid]
    }

    fun put(uuid: UUID, player: PlayerConfig) {
        cache[uuid] = player
    }

    fun flush() {
        cache.clear()
    }

    fun keys(): Set<UUID> {
        return cache.keys
    }
}