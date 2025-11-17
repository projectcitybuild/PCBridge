package com.projectcitybuild.pcbridge.paper.architecture.webhooks

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.webserver.data.WebhookEvent

class WebServerDelegate(
    private val eventBroadcaster: SpigotEventBroadcaster,
) : WebhookDelegate {
    override suspend fun handle(event: WebhookEvent) {
        log.info { "Received webhook event: $event" }

        // Pipes every webhook event into the Minecraft event broadcaster
        // so that individual features can decide whether they want to
        // handle it or not
        eventBroadcaster.broadcast(
            WebhookReceivedEvent(event),
        )
    }
}