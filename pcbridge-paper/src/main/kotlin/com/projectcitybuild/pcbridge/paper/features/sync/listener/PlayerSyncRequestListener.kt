package com.projectcitybuild.pcbridge.paper.features.sync.listener

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.features.sync.actions.SyncPlayer
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerSyncRequestListener(
    private val syncPlayer: SyncPlayer,
) : Listener {
    @EventHandler
    suspend fun onWebhookReceived(event: WebhookReceivedEvent) {
        if (event.webhook !is PlayerSyncRequestedWebhook) return;

        syncPlayer.execute(
            playerUUID = event.webhook.playerUUID,
        )
    }
}