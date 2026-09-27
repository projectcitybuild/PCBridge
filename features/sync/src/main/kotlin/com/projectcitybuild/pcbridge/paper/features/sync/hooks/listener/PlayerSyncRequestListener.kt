package com.projectcitybuild.pcbridge.paper.features.sync.hooks.listener

import com.projectcitybuild.pcbridge.paper.runtime.listeners.scoped
import com.projectcitybuild.pcbridge.paper.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.features.sync.syncTracer
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerSyncRequestListener(
    private val syncPlayer: SyncPlayer,
) : Listener {
    @EventHandler
    suspend fun onWebhookReceived(
        event: WebhookReceivedEvent,
    ) = event.scoped(syncTracer, this::class.java) {
        val webhook = event.webhook
        if (webhook !is PlayerSyncRequestedWebhook) return@scoped

        syncPlayer.execute(
            playerUUID = webhook.playerUUID,
        )
    }
}