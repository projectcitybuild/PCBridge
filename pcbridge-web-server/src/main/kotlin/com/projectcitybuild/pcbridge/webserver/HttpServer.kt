package com.projectcitybuild.pcbridge.webserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.math.BigInteger
import java.util.UUID

interface HttpServerDelegate {
    fun syncPlayer(uuid: UUID)
}

class HttpServer(
    private val config: HttpServerConfig,
    private val delegate: HttpServerDelegate,
) {
    private var server: NettyApplicationEngine? = null

    private val log
        get() = LoggerFactory.getLogger("PCBridge.WebServer")

    fun start() {
        if (config.authToken.isEmpty()) {
            log.warn("Internal web server token not set in config. Web server will not be started")
            return
        }

        log.info("Starting web server on port ${config.port}")

        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = embeddedServer(Netty, port = config.port) {
            install(CallLogging) {
                level = Level.INFO
            }
            install(Authentication) {
                bearer("token") {
                    authenticate { token ->
                        if (token.token == config.authToken) {
                            UserIdPrincipal("pcb-web")
                        } else {
                            null
                        }
                    }
                }
            }
            routing {
                authenticate("token") {
                    post("events/player/sync") {
                        val body = call.receiveParameters()

                        val rawUUID = body["uuid"]
                        if (rawUUID.isNullOrEmpty()) {
                            call.application.environment.log.info("Bad Request: No UUID provided")
                            call.respond(HttpStatusCode.BadRequest, "UUID cannot be empty")
                            return@post
                        }

                        val uuid = try {
                            uuidFromAnyString(rawUUID)
                        } catch (e: Exception) {
                            call.application.environment.log.info("Bad Request: Invalid UUID")
                            call.respond(HttpStatusCode.BadRequest, "Invalid UUID")
                            return@post
                        }

                        call.application.environment.log.info("Syncing player: $uuid")
                        delegate.syncPlayer(uuid)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        log.info("Stopping web server")

        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = null
    }
}

/**
 * Same as UUID.fromString(), except that it allows a non-hyphen string
 */
private fun uuidFromAnyString(string: String) = string.run {
    if (string.contains("-")) {
        UUID.fromString(string)
    } else {
        val bi1 = BigInteger(substring(0, 16), 16)
        val bi2 = BigInteger(substring(16, 32), 16)
        UUID(bi1.toLong(), bi2.toLong())
    }
}