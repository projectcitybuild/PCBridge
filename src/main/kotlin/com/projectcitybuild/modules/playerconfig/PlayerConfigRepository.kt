package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import java.util.*

class PlayerConfigRepository(
    private val cache: PlayerConfigCache
) {
    suspend fun get(uuid: UUID) : PlayerConfig {
        val cachedPlayer = cache.get(uuid)
        if (cachedPlayer != null) {
            return cachedPlayer
        }

        val newCachedPlayer = PlayerConfig.default(uuid)
        cache.put(uuid, newCachedPlayer)
        return newCachedPlayer
    }

    suspend fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)
    }
}