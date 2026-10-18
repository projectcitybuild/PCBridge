package com.projectcitybuild.pcbridge.features.sync.hooks.listener

import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import java.util.UUID

class PlayerSyncRequestListenerTest {
    private val syncPlayer = mock(SyncPlayer::class.java)
    private val listener = PlayerSyncRequestListener(syncPlayer)

    @Test
    fun `syncs the requested player`() =
        runTest {
            val id = UUID.randomUUID()

            listener.onWebhookReceived(WebhookReceivedEvent(WebhookEvent.PlayerSyncRequested(id)))

            verify(syncPlayer).execute(playerUUID = id)
        }

    @Test
    fun `ignores unrelated webhook events`() =
        runTest {
            listener.onWebhookReceived(WebhookReceivedEvent(WebhookEvent.SyncWarps(emptyList())))

            verify(syncPlayer, never()).execute(any())
        }
}
