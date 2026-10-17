package com.projectcitybuild.pcbridge.features.teleportation.services

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Keeps pending consent requests in memory temporarily.
 */
class TeleportRequestService(
    private val clock: Clock = Clock.systemUTC(),
    private val expiration: Duration = Duration.ofSeconds(60),
) {
    data class Recipient(
        val id: UUID,
        val name: String,
    )

    sealed interface CreateResult {
        data class Created(val request: TeleportRequest) : CreateResult

        data object SelfTarget : CreateResult

        data object AlreadyPending : CreateResult
    }

    sealed interface CreateManyResult {
        data class Created(val requests: List<TeleportRequest>) : CreateManyResult

        data object NoRecipients : CreateManyResult

        data object SelfTarget : CreateManyResult

        data object AlreadyPending : CreateManyResult
    }

    sealed interface TakeResult {
        data class Taken(val request: TeleportRequest) : TakeResult

        data object Missing : TakeResult

        data object Ambiguous : TakeResult

        data object Expired : TakeResult
    }

    private val lock = Any()

    private data class RequestKey(
        val requesterId: UUID,
        val targetId: UUID,
    )

    private val outgoing = mutableMapOf<RequestKey, TeleportRequest>()

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

        if (recipients.any { it.id == requesterId }) return@synchronized CreateManyResult.SelfTarget
        if (outgoing.values.any { it.requesterId == requesterId }) {
            return@synchronized CreateManyResult.AlreadyPending
        }
        val uniqueRecipients = recipients.distinctBy(Recipient::id)
        if (uniqueRecipients.isEmpty()) return@synchronized CreateManyResult.NoRecipients

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
            outgoing[RequestKey(request.requesterId, request.targetId)] = request
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
        if (outgoing.values.any { it.requesterId == requesterId }) {
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
        outgoing[RequestKey(requesterId, targetId)] = request
        CreateResult.Created(request)
    }

    fun takeIncoming(
        targetId: UUID,
        requesterId: UUID?,
    ): TakeResult = synchronized(lock) {
        val now = clock.instant()
        val expired = outgoing.values.filter { it.targetId == targetId && !now.isBefore(it.expiresAt) }
        removeExpired(now)

        val matchingExpired =
            expired.any {
                requesterId == null || it.requesterId == requesterId
            }
        val incoming = outgoing.values.filter { it.targetId == targetId }
        if (requesterId == null && incoming.size > 1) return@synchronized TakeResult.Ambiguous

        val request =
            if (requesterId == null) {
                incoming.singleOrNull()
            } else {
                incoming.firstOrNull { it.requesterId == requesterId }
            }
        if (request == null) {
            return@synchronized if (matchingExpired) TakeResult.Expired else TakeResult.Missing
        }

        outgoing.remove(RequestKey(request.requesterId, request.targetId))
        TakeResult.Taken(request)
    }

    fun cancel(
        requesterId: UUID,
        targetId: UUID? = null,
    ): List<TeleportRequest> = synchronized(lock) {
        removeExpired(clock.instant())
        val requests =
            outgoing.values.filter {
                it.requesterId == requesterId && (targetId == null || it.targetId == targetId)
            }
        requests.forEach { outgoing.remove(RequestKey(it.requesterId, it.targetId)) }
        requests
    }

    fun expire(
        requesterId: UUID,
        targetId: UUID,
        createdAt: Instant,
    ): TeleportRequest? = synchronized(lock) {
        val key = RequestKey(requesterId, targetId)
        val request = outgoing[key] ?: return@synchronized null
        if (request.createdAt != createdAt || clock.instant().isBefore(request.expiresAt)) {
            return@synchronized null
        }
        outgoing.remove(key)
    }

    fun disconnect(
        playerId: UUID,
    ): List<TeleportRequest> = synchronized(lock) {
        val affected = outgoing.values.filter {
            it.requesterId == playerId || it.targetId == playerId
        }
        affected.forEach { outgoing.remove(RequestKey(it.requesterId, it.targetId)) }
        affected
    }

    private fun removeExpired(now: Instant) {
        outgoing.entries.removeIf { (_, request) ->
            !now.isBefore(request.expiresAt)
        }
    }
}
