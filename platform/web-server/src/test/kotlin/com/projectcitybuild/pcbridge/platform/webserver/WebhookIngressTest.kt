package com.projectcitybuild.pcbridge.platform.webserver

import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WebhookIngressTest {
    @Test
    fun `webhook events are rejected until ready and after quiesce`() =
        runTest {
            var handled = 0
            val ingress = WebhookIngress(WebhookDelegate { handled++ })
            val event = WebhookEvent.PlayerSyncRequested(java.util.UUID.randomUUID())

            val beforeReady = runCatching { ingress.handle(event) }.exceptionOrNull()
            assertTrue(beforeReady is IllegalStateException)
            assertEquals(0, handled)

            ingress.markReady()
            ingress.handle(event)
            assertEquals(1, handled)

            ingress.quiesce()
            val afterQuiesce = runCatching { ingress.handle(event) }.exceptionOrNull()
            assertTrue(afterQuiesce is IllegalStateException)
            assertEquals(1, handled)
        }
}
