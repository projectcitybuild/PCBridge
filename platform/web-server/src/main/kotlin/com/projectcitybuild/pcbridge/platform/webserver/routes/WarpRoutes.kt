package com.projectcitybuild.pcbridge.platform.webserver.routes

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.platform.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.syncWarpRoute(delegate: WebhookDelegate) {
    post("events/warps/sync") {
        val warpNames = try {
            call.receive<List<NamedResource>>()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        call.application.environment.log.info("Received warp names (count: ${warpNames.size})")
        delegate.handle(WebhookEvent.SyncWarps(warpNames))
        call.respond(HttpStatusCode.OK)
    }
}