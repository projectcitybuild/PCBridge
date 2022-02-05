package com.projectcitybuild.features.teleporting.repositories

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Singleton
class TeleportRequestRepository {
    data class TeleportRequest(
        val requesterUUID: UUID,
        val targetUUID: UUID,
        val timerIdentifier: String,
    )

    private val requests = ConcurrentHashMap<UUID, TeleportRequest>()

    fun get(targetPlayerUUID: UUID): TeleportRequest? {
        return requests[targetPlayerUUID]
    }

    fun set(requesterUUID: UUID, targetUUID: UUID, timerIdentifier: String) {
        requests[targetUUID] = TeleportRequest(
            requesterUUID,
            targetUUID,
            timerIdentifier,
        )
    }

    fun delete(targetPlayerUUID: UUID) {
        requests.remove(targetPlayerUUID)
    }
}