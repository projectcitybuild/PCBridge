package com.projectcitybuild.pcbridge.webserver.routes

import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.webserver.data.IPBanRequestedWebhook
import com.projectcitybuild.pcbridge.webserver.data.UUIDBanRequestedWebhook
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.uuidBanRoute(delegate: WebhookDelegate) {
    post("events/ban/uuid") {
        val ban = try {
            call.receive<PlayerBan>()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        call.application.environment.log.info("Banning player: ${ban.bannedPlayer?.uuid}")
        delegate.handle(UUIDBanRequestedWebhook(ban))
        call.respond(HttpStatusCode.OK)
    }
}

fun Route.ipBanRoute(delegate: WebhookDelegate) {
    post("events/ban/ip") {
        val ban = try {
            call.receive<IPBan>()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        call.application.environment.log.info("Banning ip: ${ban.ipAddress}")
        delegate.handle(IPBanRequestedWebhook(ban))
        call.respond(HttpStatusCode.OK)
    }
}
