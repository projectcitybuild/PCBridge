package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.modules.storage.Storage
import java.util.*

/**
 * A memory cache of configs for players currently online
 * on any of the connected servers
 */
class PlayerConfigCache(
    private val storage: Storage<PlayerConfig>
) {
    private val cache = HashMap<UUID, PlayerConfig>()

    suspend fun get(uuid: UUID): PlayerConfig? {
        var memoryPlayer = cache[uuid]
        if (memoryPlayer != null) {
            return memoryPlayer
        }

        val serializedPlayer = storage.load(key = "$uuid.yml")
        if (serializedPlayer != null) {
            cache[uuid] = serializedPlayer
        }
        return serializedPlayer
    }

    suspend fun put(uuid: UUID, player: PlayerConfig) {
        cache[uuid] = player
    }

    suspend fun delete(uuid: UUID) {
        forget(uuid)
        storage.delete(uuid)
    }

    fun forget(uuid: UUID) {
        cache.remove(uuid)
    }

    fun forgetAll() {
        cache.clear()
    }
}