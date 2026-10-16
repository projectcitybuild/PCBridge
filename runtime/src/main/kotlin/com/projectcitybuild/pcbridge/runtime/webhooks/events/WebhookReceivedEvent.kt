package com.projectcitybuild.pcbridge.runtime.webhooks.events

import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
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
