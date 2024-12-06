package com.projectcitybuild.pcbridge.paper.architecture.webhooks

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import kotlinx.coroutines.test.runTest
import org.bukkit.event.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import java.util.UUID

class WebServerDelegateTest {
    @Test
    fun `pipes events into the event broadcaster`() = runTest {
        val eventBroadcaster = mock(SpigotEventBroadcaster::class.java)
        val delegate = WebServerDelegate(eventBroadcaster)

        val event = PlayerSyncRequestedWebhook(playerUUID = UUID.randomUUID())
        delegate.handle(event)

        argumentCaptor<Event>().apply {
            verify(eventBroadcaster).broadcast(capture())
            assertTrue(firstValue is WebhookReceivedEvent)
            assertEquals((firstValue as WebhookReceivedEvent).webhook, event)
        }
    }
}