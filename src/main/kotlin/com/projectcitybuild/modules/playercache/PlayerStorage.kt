package com.projectcitybuild.modules.playercache

import com.projectcitybuild.entities.CachedPlayer
import java.util.*

interface PlayerStorage {
    suspend fun load(uuid: UUID): CachedPlayer?
    suspend fun save(uuid: UUID, cachedPlayer: CachedPlayer)
    suspend fun delete(uuid: UUID)
}