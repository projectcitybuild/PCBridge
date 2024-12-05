package com.projectcitybuild.pcbridge.paper.features.config.listeners

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.webserver.data.SyncRemoteConfigWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConfigWebhookListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler
    suspend fun onConfigUpdated(event: WebhookReceivedEvent) {
        if (event.webhook !is SyncRemoteConfigWebhook) return

        remoteConfig.set(event.webhook.config)
    }
}