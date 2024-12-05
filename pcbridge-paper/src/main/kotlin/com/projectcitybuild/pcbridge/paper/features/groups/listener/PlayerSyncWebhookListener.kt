package com.projectcitybuild.pcbridge.paper.features.groups.listener

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerSyncWebhookListener(
    private val eventBroadcaster: SpigotEventBroadcaster,
) : Listener {
    @EventHandler
    suspend fun onSyncRequested(event: WebhookReceivedEvent) {
        if (event.webhook !is PlayerSyncRequestedWebhook) return;

        // Already handled by a separate listener - bridge it to the expected event
        eventBroadcaster.broadcast(
            PlayerSyncRequestedEvent(playerUUID = event.webhook.playerUUID),
        )
    }
}
