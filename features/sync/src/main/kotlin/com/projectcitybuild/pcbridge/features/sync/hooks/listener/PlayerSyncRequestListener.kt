package com.projectcitybuild.pcbridge.features.sync.hooks.listener

import com.projectcitybuild.pcbridge.runtime.listeners.traced
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.features.sync.syncTracer
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerSyncRequestListener(
    private val syncPlayer: SyncPlayer,
) : Listener {
    @EventHandler
    suspend fun onWebhookReceived(
        event: WebhookReceivedEvent,
    ) = event.traced(syncTracer, this::class.java) {
        val webhook = event.webhook
        if (webhook !is WebhookEvent.PlayerSyncRequested) return@traced

        syncPlayer.execute(
            playerUUID = webhook.playerUUID,
        )
    }
}