package com.projectcitybuild.pcbridge.paper.architecture.webhooks.events

import com.projectcitybuild.pcbridge.webserver.data.WebhookEvent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class WebhookReceivedEvent(
    val webhook: WebhookEvent,
) : Event() {
    override fun getHandlers() = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
