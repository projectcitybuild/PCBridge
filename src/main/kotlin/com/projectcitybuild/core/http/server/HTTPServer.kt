package com.projectcitybuild.core.http.server

import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.support.spigot.logger.Logger
import com.projectcitybuild.support.spigot.scheduler.Scheduler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.bukkit.Server
import javax.inject.Inject

class HTTPServer @Inject constructor(
    private val scheduler: Scheduler,
    private val minecraftServer: Server,
    private val config: Config,
    private val logger: Logger,
) {
    private var cancellable: Cancellable? = null

    fun run() {
        val bearer = config.get(ConfigKeys.internalWebServerToken)
        if (bearer.isEmpty()) {
            logger.fatal("Internal web server token not set in config. Web server will not be started")
            return
        }

        val task = scheduler.async<Unit> {
            embeddedServer(Netty, port = 8080) {
                routing {
                    get("/") {
                        call.respond(HttpStatusCode.OK)
                        logger.info("Received HTTP connection")
                    }
                    get("player/sync") {
                        val token = call.request.headers.get("Authorization")
                        if (token.isNullOrEmpty() || token != "Bearer $bearer") {
                            call.respond(HttpStatusCode.Unauthorized)
                            return@get
                        }

                        logger.info("Received HTTP connection")

                        scheduler.sync {
                            minecraftServer.onlinePlayers.forEach { it.sendMessage("HTTP ping") }
                        }

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }.start(wait = true)
        }
        cancellable = task.start()
    }

    fun stop() {
        cancellable?.cancel()
    }
}