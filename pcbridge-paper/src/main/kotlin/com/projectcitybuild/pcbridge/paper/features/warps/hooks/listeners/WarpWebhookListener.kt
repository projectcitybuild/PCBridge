package com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.webserver.data.SyncWarpsWebhook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpWebhookListener(
    private val warpRepository: WarpRepository,
) : Listener {
    @EventHandler
    fun onWarpsUpdated(event: WebhookReceivedEvent) {
        if (event.webhook !is SyncWarpsWebhook) return

        warpRepository.setNames(event.webhook.warpNames)
    }
}