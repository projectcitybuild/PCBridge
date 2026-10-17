package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportRequest
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportRequestService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class TeleportRequestServiceTest {
    private val start = Instant.parse("2026-10-16T12:00:00Z")

    @Test
    fun `request creation prevents self target and one active outgoing request`() {
        val service = TeleportRequestService()
        val requester = UUID.randomUUID()
        val target = UUID.randomUUID()

        assertEquals(
            TeleportRequestService.CreateResult.SelfTarget,
            service.create(requester, "Alex", requester, "Alex", false),
        )
        val request =
            assertInstanceOf(
                TeleportRequestService.CreateResult.Created::class.java,
                service.create(requester, "Alex", target, "Blair", true),
            ).request

        assertEquals(request.createdAt.plusSeconds(60), request.expiresAt)
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

        val created =
            assertInstanceOf(
                TeleportRequestService.CreateManyResult.Created::class.java,
                service.createHereForMany(
                    requesterId = requester,
                    requesterName = "Alex",
                    recipients =
                        listOf(
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
        service.create(firstRequester, "Alex", target, "Blair", false)
        service.create(secondRequester, "Casey", target, "Blair", false)

        assertEquals(
            TeleportRequestService.TakeResult.Ambiguous,
            service.takeIncoming(target, null),
        )
        val taken =
            assertInstanceOf(
                TeleportRequestService.TakeResult.Taken::class.java,
                service.takeIncoming(target, firstRequester),
            ).request

        assertEquals(firstRequester, taken.requesterId)
        assertEquals(TeleportRequestService.TakeResult.Missing, service.takeIncoming(target, firstRequester))
        assertEquals(
            secondRequester,
            assertInstanceOf(
                TeleportRequestService.TakeResult.Taken::class.java,
                service.takeIncoming(target, null),
            ).request.requesterId,
        )
    }

    @Test
    fun `cancel expiration and disconnect remove pending requests`() {
        val clock = MutableClock(start)
        val service = TeleportRequestService(clock = clock)
        val requester = UUID.randomUUID()
        val target = UUID.randomUUID()
        val created =
            assertInstanceOf(
                TeleportRequestService.CreateResult.Created::class.java,
                service.create(requester, "Alex", target, "Blair", false),
            ).request

        assertEquals(listOf(created), service.cancel(requester))
        assertEquals(emptyList<TeleportRequest>(), service.cancel(requester))

        service.create(requester, "Alex", target, "Blair", false)
        clock.current = start.plusSeconds(60)
        assertEquals(TeleportRequestService.TakeResult.Expired, service.takeIncoming(target, null))
        assertNull(service.expire(requester, target, createdAt = start))

        val request =
            assertInstanceOf(
                TeleportRequestService.CreateResult.Created::class.java,
                service.create(requester, "Alex", target, "Blair", false),
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
        val request =
            assertInstanceOf(
                TeleportRequestService.CreateResult.Created::class.java,
                service.create(requester, "Alex", target, "Blair", false),
            ).request

        clock.current = request.expiresAt
        assertEquals(request, service.expire(requester, target, request.createdAt))
        assertEquals(TeleportRequestService.TakeResult.Missing, service.takeIncoming(target, null))
    }

    private class MutableClock(
        var current: Instant,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")

        override fun withZone(zone: ZoneId): Clock = this

        override fun instant(): Instant = current
    }
}
