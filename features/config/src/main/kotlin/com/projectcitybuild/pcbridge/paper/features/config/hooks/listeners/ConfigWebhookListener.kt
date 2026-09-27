package com.projectcitybuild.pcbridge.paper.features.config.hooks.listeners

import com.projectcitybuild.pcbridge.paper.features.config.configTracer
import com.projectcitybuild.pcbridge.paper.runtime.listeners.scoped
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.webserver.data.SyncRemoteConfigWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConfigWebhookListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler
    suspend fun onConfigUpdated(event: WebhookReceivedEvent) =
        event.scoped(configTracer, this::class.java) {
            val webhook = event.webhook
            if (webhook !is SyncRemoteConfigWebhook) return@scoped

            remoteConfig.set(webhook.config)
        }
}
