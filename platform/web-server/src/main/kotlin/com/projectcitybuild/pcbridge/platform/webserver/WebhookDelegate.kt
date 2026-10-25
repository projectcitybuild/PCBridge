package com.projectcitybuild.pcbridge.platform.webserver

import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent

fun interface WebhookDelegate {
    suspend fun handle(event: WebhookEvent)
}

class WebhookIngress(
    private val delegate: WebhookDelegate,
) : WebhookDelegate {
    @Volatile
    private var ready = false

    val isReady: Boolean
        get() = ready

    fun markReady() {
        ready = true
    }

    fun quiesce() {
        ready = false
    }

    override suspend fun handle(event: WebhookEvent) {
        check(ready) { "Webhook ingress is not ready" }
        delegate.handle(event)
    }
}