package com.projectcitybuild.repositories

import com.projectcitybuild.entities.responses.Badge
import dagger.Reusable
import java.util.UUID
import javax.inject.Inject

@Reusable
class ChatBadgeRepository @Inject constructor() {
    private val cache: MutableMap<UUID, List<Badge>> = mutableMapOf()

    fun put(playerUUID: UUID, badges: List<Badge>) {
        cache[playerUUID] = badges
    }

    fun get(playerUUID: UUID): List<Badge> {
        return cache[playerUUID] ?: emptyList()
    }

    fun remove(playerUUID: UUID) {
        cache.remove(playerUUID)
    }
}
