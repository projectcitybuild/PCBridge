package com.projectcitybuild.modules.playercache

import com.projectcitybuild.entities.CachedPlayer
import java.util.*

class PlayerCache(
    private val storage: PlayerStorage
) {
    private val cache = HashMap<UUID, CachedPlayer>()

    suspend fun get(uuid: UUID): CachedPlayer? {
        var memoryPlayer = cache[uuid]
        if (memoryPlayer != null) {
            return memoryPlayer
        }

        val serializedPlayer = storage.load(uuid)
        if (serializedPlayer != null) {
            cache[uuid] = serializedPlayer
        }
        return serializedPlayer
    }

    suspend fun put(uuid: UUID, player: CachedPlayer) {
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