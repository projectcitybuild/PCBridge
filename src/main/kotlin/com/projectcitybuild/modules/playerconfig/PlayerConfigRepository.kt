package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.modules.storage.Storage
import java.util.*

class PlayerConfigRepository(
    private val cache: PlayerConfigCache,
    private val storage: Storage<PlayerConfig>
) {
    suspend fun get(uuid: UUID) : PlayerConfig {
        val cachedPlayer = cache.get(uuid)
        if (cachedPlayer != null) {
            return cachedPlayer
        }

        val serializedPlayer = storage.load(uuid.toString())
        if (serializedPlayer != null) {
            cache.put(uuid, serializedPlayer)
            return serializedPlayer
        }

        val newCachedPlayer = PlayerConfig.default(uuid)
        save(newCachedPlayer)
        return newCachedPlayer
    }

    suspend fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)
        storage.save(player.uuid.toString(), player)
    }
}