package com.projectcitybuild.pcbridge.platform.webserver

import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent

interface WebhookDelegate {
    suspend fun handle(event: WebhookEvent)
}