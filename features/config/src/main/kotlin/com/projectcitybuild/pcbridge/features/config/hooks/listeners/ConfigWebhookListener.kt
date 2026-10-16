package com.projectcitybuild.pcbridge.features.config.hooks.listeners

import com.projectcitybuild.pcbridge.features.config.configTracer
import com.projectcitybuild.pcbridge.runtime.listeners.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConfigWebhookListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler
    suspend fun onConfigUpdated(event: WebhookReceivedEvent) =
        event.scoped(configTracer, this::class.java) {
            val webhook = event.webhook
            if (webhook !is WebhookEvent.SyncRemoteConfig) return@scoped

            remoteConfig.set(webhook.config)
        }
}
