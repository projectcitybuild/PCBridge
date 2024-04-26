package com.projectcitybuild.libs.playercache

import com.projectcitybuild.entities.PlayerConfig
import java.util.UUID

/**
 * A PlayerConfig memory cache for players currently online
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

    fun remove(uuid: UUID) {
        cache.remove(uuid)
    }

    fun flush() {
        cache.clear()
    }
}
