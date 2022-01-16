package com.projectcitybuild.old_modules.playerconfig

import com.projectcitybuild.entities.LegacyPlayerConfig
import java.util.*

class LegacyPlayerConfigRepository(
    private val cache: LegacyPlayerConfigCache,
    private val storage: PlayerConfigFileStorage
) {
    suspend fun get(uuid: UUID) : LegacyPlayerConfig {
        val cachedPlayer = cache.get(uuid)
        if (cachedPlayer != null) {
            return cachedPlayer
        }

        val serializedPlayer = storage.load(uuid.toString())
        if (serializedPlayer != null) {
            cache.put(uuid, serializedPlayer)
            return serializedPlayer
        }

        val newCachedPlayer = LegacyPlayerConfig.default(uuid)
        save(newCachedPlayer)
        return newCachedPlayer
    }

    suspend fun save(player: LegacyPlayerConfig) {
        cache.put(player.uuid.unwrapped, player)
        storage.save(player.uuid.toString(), player)
    }
}