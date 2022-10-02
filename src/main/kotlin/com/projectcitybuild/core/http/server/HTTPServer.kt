package com.projectcitybuild.core.http.server

import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroups
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.support.spigot.logger.Logger
import com.projectcitybuild.support.spigot.scheduler.Scheduler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Server
import javax.inject.Inject

class HTTPServer @Inject constructor(
    private val scheduler: Scheduler,
    private val minecraftServer: Server,
    private val config: Config,
    private val logger: Logger,
    private val updatePlayerGroups: UpdatePlayerGroups,
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
                    get("player/{uuid}/sync") {
                        logger.info("Received HTTP connection: ${call.request.uri}")

                        val token = call.request.headers.get("Authorization")
                        if (token.isNullOrEmpty() || token != "Bearer $bearer") {
                            logger.info("401 Unauthorized: No token or invalid token provided")
                            call.respond(HttpStatusCode.Unauthorized)
                            return@get
                        }

                        val rawUUID = call.parameters.get("uuid")
                        if (rawUUID.isNullOrEmpty()) {
                            logger.info("400 Bad Request: No UUID provided")
                            call.respond(HttpStatusCode.BadRequest, "UUID cannot be empty")
                            return@get
                        }

                        scheduler.sync {
                            val player = minecraftServer.onlinePlayers.firstOrNull {
                                it.uniqueId.toString().unformatted() == rawUUID.unformatted()
                            } ?: return@sync

                            scheduler.async<Unit> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    updatePlayerGroups.execute(playerUUID = player.uniqueId)
                                }
                            }
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

private fun String.unformatted(): String {
    return lowercase().replace(oldValue = "-", newValue = "")
}
