package com.projectcitybuild.pcbridge.paper.features.config.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.config.configTracer
import com.projectcitybuild.pcbridge.webserver.data.SyncRemoteConfigWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConfigWebhookListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler
    suspend fun onConfigUpdated(
        event: WebhookReceivedEvent,
    ) = event.scoped(configTracer, this::class.java) {
        if (event.webhook !is SyncRemoteConfigWebhook) return@scoped

        remoteConfig.set(event.webhook.config)
    }
}