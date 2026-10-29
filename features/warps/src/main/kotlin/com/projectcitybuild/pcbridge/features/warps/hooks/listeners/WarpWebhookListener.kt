package com.projectcitybuild.pcbridge.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpWebhookListener(
    private val warpRepository: WarpRepository,
) : Listener {
    @EventHandler
    fun onWarpsUpdated(event: WebhookReceivedEvent) =
        event.tracedSync(warpsTracer, this::class.java) {
            val webhook = event.webhook
            if (webhook !is WebhookEvent.SyncWarps) return@tracedSync

            warpRepository.setNames(webhook.warpNames)
        }
}
