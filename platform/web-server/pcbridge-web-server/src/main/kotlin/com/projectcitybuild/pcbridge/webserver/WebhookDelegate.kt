package com.projectcitybuild.pcbridge.webserver

import com.projectcitybuild.pcbridge.webserver.data.WebhookEvent

interface WebhookDelegate {
    suspend fun handle(event: WebhookEvent)
}