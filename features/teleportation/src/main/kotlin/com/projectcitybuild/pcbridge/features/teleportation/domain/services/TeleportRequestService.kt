package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportRequest
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Keeps pending consent requests in memory temporarily.
 */
class TeleportRequestService(
    private val clock: Clock = Clock.systemUTC(),
    private val expiration: Duration = Duration.ofSeconds(30),
) {
    data class Recipient(
        val id: UUID,
        val name: String,
    )

    sealed interface CreateResult {
        data class Created(
            val request: TeleportRequest,
        ) : CreateResult

        data object SelfTarget : CreateResult

        data object AlreadyPending : CreateResult
    }

    sealed interface CreateManyResult {
        data class Created(
            val requests: List<TeleportRequest>,
        ) : CreateManyResult

        data object NoRecipients : CreateManyResult

        data object SelfTarget : CreateManyResult

        data object AlreadyPending : CreateManyResult
    }

    sealed interface TakeResult {
        data class Taken(
            val request: TeleportRequest,
        ) : TakeResult

        data object Missing : TakeResult

        data object Ambiguous : TakeResult

        data object Expired : TakeResult
    }

    private val lock = Any()

    private data class RequestKey(
        val requesterId: UUID,
        val targetId: UUID,
    )

    private val pendingRequests = mutableMapOf<RequestKey, TeleportRequest>()

    fun create(
        requesterId: UUID,
        requesterName: String,
        targetId: UUID,
        targetName: String,
        silent: Boolean,
    ): CreateResult = create(
        requesterId = requesterId,
        requesterName = requesterName,
        targetId = targetId,
        targetName = targetName,
        silent = silent,
        direction = TeleportRequest.Direction.TO,
    )

    fun createHere(
        requesterId: UUID,
        requesterName: String,
        targetId: UUID,
        targetName: String,
        silent: Boolean,
    ): CreateResult = create(
        requesterId = requesterId,
        requesterName = requesterName,
        targetId = targetId,
        targetName = targetName,
        silent = silent,
        direction = TeleportRequest.Direction.HERE,
    )

    fun createHereForMany(
        requesterId: UUID,
        requesterName: String,
        recipients: List<Recipient>,
        silent: Boolean,
    ): CreateManyResult = synchronized(lock) {
        val now = clock.instant()
        removeExpired(now)

        if (recipients.any { it.id == requesterId }) {
            return@synchronized CreateManyResult.SelfTarget
        }
        if (pendingRequests.values.any { it.requesterId == requesterId }) {
            return@synchronized CreateManyResult.AlreadyPending
        }
        val uniqueRecipients = recipients.distinctBy(Recipient::id)
        if (uniqueRecipients.isEmpty()) {
            return@synchronized CreateManyResult.NoRecipients
        }

        val requests = uniqueRecipients.map { recipient ->
            TeleportRequest(
                requesterId = requesterId,
                requesterName = requesterName,
                targetId = recipient.id,
                targetName = recipient.name,
                createdAt = now,
                expiresAt = now.plus(expiration),
                silent = silent,
                direction = TeleportRequest.Direction.HERE,
            )
        }
        requests.forEach { request ->
            pendingRequests[RequestKey(request.requesterId, request.targetId)] = request
        }
        CreateManyResult.Created(requests)
    }

    private fun create(
        requesterId: UUID,
        requesterName: String,
        targetId: UUID,
        targetName: String,
        silent: Boolean,
        direction: TeleportRequest.Direction,
    ): CreateResult = synchronized(lock) {
        val now = clock.instant()
        removeExpired(now)

        if (requesterId == targetId) {
            return@synchronized CreateResult.SelfTarget
        }
        if (pendingRequests.values.any { it.requesterId == requesterId }) {
            return@synchronized CreateResult.AlreadyPending
        }

        val request = TeleportRequest(
            requesterId = requesterId,
            requesterName = requesterName,
            targetId = targetId,
            targetName = targetName,
            createdAt = now,
            expiresAt = now.plus(expiration),
            silent = silent,
            direction = direction,
        )
        pendingRequests[RequestKey(requesterId, targetId)] = request
        CreateResult.Created(request)
    }

    fun takeIncoming(
        targetId: UUID,
        requesterId: UUID?,
    ): TakeResult = synchronized(lock) {
        val now = clock.instant()
        val matching = pendingRequests.values.filter {
            it.targetId == targetId && (requesterId == null || it.requesterId == requesterId)
        }
        val matchingExpired = matching.any { it.expiresAt <= now }

        removeExpired(now)

        val incoming = pendingRequests.values.filter {
            it.targetId == targetId
        }
        if (requesterId == null && incoming.size > 1) {
            return@synchronized TakeResult.Ambiguous
        }
        val request = if (requesterId != null) {
            incoming.firstOrNull { it.requesterId == requesterId }
        } else {
            incoming.singleOrNull()
        }
        if (request == null) {
            return@synchronized if (matchingExpired) {
                TakeResult.Expired
            } else {
                TakeResult.Missing
            }
        }

        val key = RequestKey(request.requesterId, request.targetId)
        pendingRequests.remove(key)

        TakeResult.Taken(request)
    }

    fun cancel(
        requesterId: UUID,
        targetId: UUID? = null,
    ): List<TeleportRequest> = synchronized(lock) {
        removeExpired(clock.instant())
        val requests = pendingRequests.values.filter {
            it.requesterId == requesterId && (targetId == null || it.targetId == targetId)
        }
        requests.forEach {
            val key = RequestKey(it.requesterId, it.targetId)
            pendingRequests.remove(key)
        }
        requests
    }

    fun expire(
        requesterId: UUID,
        targetId: UUID,
        createdAt: Instant,
    ): TeleportRequest? = synchronized(lock) {
        val key = RequestKey(requesterId, targetId)
        val request = pendingRequests[key] ?: return@synchronized null
        val requestWasReplaced = request.createdAt != createdAt
        if (requestWasReplaced) {
            return@synchronized null
        }
        pendingRequests.remove(key)
    }

    fun disconnect(playerId: UUID): List<TeleportRequest> = synchronized(lock) {
        val affected = pendingRequests.values.filter {
            it.requesterId == playerId || it.targetId == playerId
        }
        affected.forEach {
            val key = RequestKey(it.requesterId, it.targetId)
            pendingRequests.remove(key)
        }
        affected
    }

    private fun removeExpired(now: Instant) {
        pendingRequests.entries.removeIf { (_, request) ->
            request.expiresAt <= now
        }
    }
}
