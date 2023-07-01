package com.projectcitybuild.pcbridge.webserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import com.projectcitybuild.pcbridge.core.PlatformLogger

interface HttpServerDelegate {
    fun syncPlayer(uuid: String)
}

class HttpServer(
    private val config: HttpServerConfig,
    private val delegate: HttpServerDelegate,
    private val logger: PlatformLogger,
) {
    private var server: NettyApplicationEngine? = null

    fun start() {
        if (config.authToken.isEmpty()) {
            logger.fatal("Internal web server token not set in config. Web server will not be started")
            return
        }

        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = embeddedServer(Netty, port = config.port) {
            routing {
                get("player/{uuid}/sync") {
                    logger.info("Received HTTP connection: ${call.request.uri}")

                    val token = call.request.headers.get("Authorization")
                    if (token.isNullOrEmpty() || token != "Bearer $config.authToken") {
                        logger.info("401 Unauthorized: No token or invalid token provided")
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }

                    val rawUUID = call.parameters["uuid"]
                    if (rawUUID.isNullOrEmpty()) {
                        logger.info("400 Bad Request: No UUID provided")
                        call.respond(HttpStatusCode.BadRequest, "UUID cannot be empty")
                        return@get
                    }

                    delegate.syncPlayer(rawUUID)

                    call.respond(HttpStatusCode.OK)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = null
    }
}
