package com.projectcitybuild.pcbridge.platform.webserver.routes

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.syncConfigRoute(delegate: WebhookDelegate) {
    post("events/config") {
        val config = try {
            call.receive<RemoteConfigVersion>()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        call.application.environment.log.info("Received config version ${config.version}")
        delegate.handle(WebhookEvent.SyncRemoteConfig(config))
        call.respond(HttpStatusCode.OK)
    }
}