package com.projectcitybuild.repositories

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeleportRequestRepository @Inject constructor() {
    data class TeleportRequest(
        val requesterUUID: UUID,
        val targetUUID: UUID,
        val timerIdentifier: String,
        val teleportType: TeleportType,
    )

    enum class TeleportType {
        TP_TO_PLAYER,
        SUMMON_PLAYER,
    }

    private val requests = ConcurrentHashMap<UUID, TeleportRequest>()

    fun get(targetPlayerUUID: UUID): TeleportRequest? {
        return requests[targetPlayerUUID]
    }

    fun set(requesterUUID: UUID, targetUUID: UUID, timerIdentifier: String, teleportType: TeleportType) {
        requests[targetUUID] = TeleportRequest(
            requesterUUID,
            targetUUID,
            timerIdentifier,
            teleportType,
        )
    }

    fun delete(targetPlayerUUID: UUID) {
        requests.remove(targetPlayerUUID)
    }
}
