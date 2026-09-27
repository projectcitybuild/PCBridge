package com.projectcitybuild.pcbridge.webserver.routes

import com.projectcitybuild.pcbridge.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import com.projectcitybuild.pcbridge.webserver.data.requests.PlayerSyncRequest
import com.projectcitybuild.pcbridge.webserver.utils.uuidFromAnyString
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.CannotTransformContentToTypeException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.playerSyncRoute(webhookDelegate: WebhookDelegate) {
    post("events/player/sync") {
        val body = try {
            call.receive<PlayerSyncRequest>()
        } catch (e: CannotTransformContentToTypeException) {
            call.application.environment.log.info("Bad Request: ${e.message}")
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            return@post
        }
        val uuid = try {
            uuidFromAnyString(body.uuid)
        } catch (e: Exception) {
            call.application.environment.log.info("Bad Request: Invalid UUID")
            call.respond(HttpStatusCode.BadRequest, "Invalid UUID")
            return@post
        }
        call.application.environment.log.info("Syncing player: $uuid")
        webhookDelegate.handle(PlayerSyncRequestedWebhook(uuid))
        call.respond(HttpStatusCode.OK)
    }
}