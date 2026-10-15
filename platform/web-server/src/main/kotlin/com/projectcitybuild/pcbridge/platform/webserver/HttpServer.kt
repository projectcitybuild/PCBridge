package com.projectcitybuild.pcbridge.platform.webserver

import com.projectcitybuild.pcbridge.core.http.serialization.gson.LocalDateTimeTypeAdapter
import com.projectcitybuild.pcbridge.platform.webserver.data.HttpServerConfig
import com.projectcitybuild.pcbridge.platform.webserver.routes.ipBanRoute
import com.projectcitybuild.pcbridge.platform.webserver.routes.playerSyncRoute
import com.projectcitybuild.pcbridge.platform.webserver.routes.syncConfigRoute
import com.projectcitybuild.pcbridge.platform.webserver.routes.syncWarpRoute
import com.projectcitybuild.pcbridge.platform.webserver.routes.uuidBanRoute
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.time.LocalDateTime

class HttpServer(
    private val config: HttpServerConfig,
    private val webhookDelegate: WebhookDelegate,
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    private val log
        get() = LoggerFactory.getLogger("com.projectcitybuild.pcbridge.webserver")

    fun start() {
        require(config.authToken.isNotBlank()) {
            "Webhook authentication token must be configured"
        }
        require(config.authToken != "REPLACE_WITH_A_RANDOM_SECRET") {
            "The default webhook token must not be used"
        }

        log.info("Starting web server at ${config.host}:${config.port}")

        if (config.host == "127.0.0.1") {
            log.info("Web server will only be accessible locally")
        }

        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = embeddedServer(Netty, port = config.port, host = config.host) {
            configureLogging()
            configureContentNegotiation()
            configureAuthentication(authToken = config.authToken)
            configureRouting(webhookDelegate)

        }.start(wait = false)
    }

    fun stop() {
        log.info("Stopping web server")

        server?.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        server = null
    }
}

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO // Logs calls as info level
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        }
    }
}

fun Application.configureAuthentication(authToken: String) {
    install(Authentication) {
        bearer("token") {
            authenticate { credentials ->
                if (credentials.token == authToken) {
                    UserIdPrincipal("pcb-web")
                } else {
                    null
                }
            }
        }
    }
}

fun Application.configureRouting(webhookDelegate: WebhookDelegate) {
    routing {
        authenticate("token") {
            playerSyncRoute(webhookDelegate)
            uuidBanRoute(webhookDelegate)
            ipBanRoute(webhookDelegate)
            syncConfigRoute(webhookDelegate)
            syncWarpRoute(webhookDelegate)
        }
    }
}
