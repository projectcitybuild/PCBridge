package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.responses.Badge
import java.util.UUID

class ChatBadgeRepository {
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
