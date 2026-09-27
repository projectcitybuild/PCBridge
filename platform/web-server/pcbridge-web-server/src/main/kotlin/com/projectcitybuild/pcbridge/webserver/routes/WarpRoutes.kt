package com.projectcitybuild.pcbridge.webserver.routes

import com.projectcitybuild.pcbridge.http.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.webserver.data.SyncWarpsWebhook
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
        delegate.handle(SyncWarpsWebhook(warpNames))
        call.respond(HttpStatusCode.OK)
    }
}