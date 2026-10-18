package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.projectcitybuild.pcbridge.core.testsupport.MutableClock
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class TeleportRequestServiceTest {
    private val start = Instant.parse("2026-10-16T12:00:00Z")

    @Test
    fun `request creation prevents self target`() {
        val service = TeleportRequestService()
        val requester = UUID.randomUUID()

        assertEquals(
            TeleportRequestService.CreateResult.SelfTarget,
            service.create(
                requesterId = requester,
                requesterName = "Alex",
                targetId = requester,
                targetName = "Alex",
                silent = false,
            ),
        )
    }

    @Test
    fun `request creation prevents more than one active outgoing request`() {
        val service = TeleportRequestService()
        val requester = UUID.randomUUID()
        val target = UUID.randomUUID()

        val request = assertInstanceOf(
            TeleportRequestService.CreateResult.Created::class.java,
            service.create(requester, "Alex", target, "Blair", true),
        ).request

        assertEquals(true, request.silent)
        assertEquals(
            TeleportRequestService.CreateResult.AlreadyPending,
            service.create(requester, "Alex", UUID.randomUUID(), "Casey", false),
        )
    }

    @Test
    fun `batch here requests allow one requester to target multiple players`() {
        val service = TeleportRequestService()
        val requester = UUID.randomUUID()
        val firstTarget = UUID.randomUUID()
        val secondTarget = UUID.randomUUID()

        val created = assertInstanceOf(
            TeleportRequestService.CreateManyResult.Created::class.java,
            service.createHereForMany(
                requesterId = requester,
                requesterName = "Alex",
                recipients = listOf(
                    TeleportRequestService.Recipient(firstTarget, "Blair"),
                    TeleportRequestService.Recipient(secondTarget, "Casey"),
                ),
                silent = false,
            ),
        )
        assertEquals(2, created.requests.size)
        assertInstanceOf(
            TeleportRequestService.TakeResult.Taken::class.java,
            service.takeIncoming(firstTarget, requester),
        )
        assertEquals(
            listOf(created.requests.single { it.targetId == secondTarget }),
            service.cancel(requester, secondTarget),
        )
        assertEquals(emptyList<TeleportRequest>(), service.cancel(requester))
    }

    @Test
    fun `take incoming detects ambiguity and consumes a request only once`() {
        val service = TeleportRequestService()
        val target = UUID.randomUUID()
        val firstRequester = UUID.randomUUID()
        val secondRequester = UUID.randomUUID()
        service.create(
            requesterId = firstRequester,
            requesterName = "Alex",
            targetId = target,
            targetName = "Blair",
            silent = false,
        )
        service.create(
            requesterId = secondRequester,
            requesterName = "Casey",
            targetId = target,
            targetName = "Blair",
            silent = false,
        )

        assertEquals(
            TeleportRequestService.TakeResult.Ambiguous,
            service.takeIncoming(target, null),
        )
        val taken = assertInstanceOf(
            TeleportRequestService.TakeResult.Taken::class.java,
            service.takeIncoming(target, firstRequester),
        ).request

        assertEquals(firstRequester, taken.requesterId)
        assertEquals(
            TeleportRequestService.TakeResult.Missing,
            service.takeIncoming(
                targetId = target,
                requesterId = firstRequester,
            ),
        )
        assertEquals(
            secondRequester,
            assertInstanceOf(
                TeleportRequestService.TakeResult.Taken::class.java,
                service.takeIncoming(
                    targetId = target,
                    requesterId = null,
                ),
            ).request.requesterId,
        )
    }

    @Test
    fun `cancel expiration and disconnect remove pending requests`() {
        val clock = MutableClock(start)
        val service = TeleportRequestService(
            clock = clock,
            expiration = Duration.ofSeconds(30),
        )
        val requester = UUID.randomUUID()
        val target = UUID.randomUUID()
        val created = assertInstanceOf(
            TeleportRequestService.CreateResult.Created::class.java,
            service.create(
                requesterId = requester,
                requesterName = "Alex",
                targetId = target,
                targetName = "Blair",
                silent = false,
            ),
        ).request

        assertEquals(listOf(created), service.cancel(requester))
        assertEquals(emptyList<TeleportRequest>(), service.cancel(requester))

        service.create(
            requesterId = requester,
            requesterName = "Alex",
            targetId = target,
            targetName = "Blair",
            silent = false,
        )
        clock.current = start.plusSeconds(30)
        assertEquals(
            TeleportRequestService.TakeResult.Expired,
            service.takeIncoming(targetId = target, requesterId = null),
        )
        assertNull(
            service.expire(
                requesterId = requester,
                targetId = target,
                createdAt = start,
            ),
        )

        val request = assertInstanceOf(
            TeleportRequestService.CreateResult.Created::class.java,
            service.create(
                requesterId = requester,
                requesterName = "Alex",
                targetId = target,
                targetName = "Blair",
                silent = false,
            ),
        ).request
        assertEquals(listOf(request), service.disconnect(target))
        assertEquals(TeleportRequestService.TakeResult.Missing, service.takeIncoming(target, null))
    }

    @Test
    fun `scheduled expiration removes the matching request`() {
        val clock = MutableClock(start)
        val service = TeleportRequestService(clock = clock, expiration = Duration.ofSeconds(60))
        val requester = UUID.randomUUID()
        val target = UUID.randomUUID()
        val request = assertInstanceOf(
            TeleportRequestService.CreateResult.Created::class.java,
            service.create(
                requesterId = requester,
                requesterName = "Alex",
                targetId = target,
                targetName = "Blair",
                silent = false,
            ),
        ).request

        clock.current = request.expiresAt
        assertEquals(
            request,
            service.expire(
                requesterId = requester,
                targetId = target,
                createdAt = request.createdAt,
            ),
        )
        assertEquals(
            TeleportRequestService.TakeResult.Missing,
            service.takeIncoming(target, null),
        )
    }
}
