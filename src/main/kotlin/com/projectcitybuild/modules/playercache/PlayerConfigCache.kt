package com.projectcitybuild.modules.playercache

import com.projectcitybuild.entities.PlayerConfig
import dagger.Reusable
import java.util.UUID
import javax.inject.Inject

/**
 * A PlayerConfig memory cache for players currently online
 * on any of the connected servers
 */
@Reusable
class PlayerConfigCache @Inject constructor() {
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
