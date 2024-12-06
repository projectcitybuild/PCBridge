package com.projectcitybuild.pcbridge.paper.features.warps.listeners

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.webserver.data.SyncWarpsWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpWebhookListener(
    private val warpRepository: WarpRepository,
) : Listener {
    @EventHandler
    suspend fun onWarpsUpdated(event: WebhookReceivedEvent) {
        if (event.webhook !is SyncWarpsWebhook) return

        warpRepository.reload()
    }
}