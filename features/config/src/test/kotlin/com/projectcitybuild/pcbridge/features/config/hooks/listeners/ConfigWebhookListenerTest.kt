package com.projectcitybuild.pcbridge.features.config.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class ConfigWebhookListenerTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val listener = ConfigWebhookListener(remoteConfig)

    @Test
    fun `applies a remote config sync webhook`() =
        runTest {
            val config = RemoteConfigVersion(7, RemoteConfigKeyValues(motd = "updated"))

            listener.onConfigUpdated(WebhookReceivedEvent(WebhookEvent.SyncRemoteConfig(config)))

            verify(remoteConfig).set(config)
        }

    @Test
    fun `ignores unrelated webhooks`() =
        runTest {
            listener.onConfigUpdated(WebhookReceivedEvent(WebhookEvent.SyncWarps(emptyList())))

            verify(remoteConfig, never()).set(org.mockito.kotlin.any(), org.mockito.kotlin.any())
        }
}
