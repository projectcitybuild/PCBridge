package com.projectcitybuild.pcbridge.webserver

import com.projectcitybuild.pcbridge.http.models.IPBan
import com.projectcitybuild.pcbridge.http.models.PlayerBan
import com.projectcitybuild.pcbridge.http.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.models.Warp
import com.projectcitybuild.pcbridge.http.serialization.gson.LocalDateTimeTypeAdapter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
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
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.UUID

interface HttpServerDelegate {
    suspend fun syncPlayer(uuid: UUID)
    suspend fun banPlayer(ban: PlayerBan)
    suspend fun banIP(ban: IPBan)
    suspend fun updateConfig(config: RemoteConfigVersion)
    suspend fun syncWarps(warps: List<Warp>)
}

class HttpServer(
    private val config: HttpServerConfig,
    private val delegate: HttpServerDelegate,
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

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
                level = Level.INFO // Logs calls as info level
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
            install(ContentNegotiation) {
                gson {
                    registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
                }
            }

            routing {
                authenticate("token") {
                    post("events/player/sync") {
                        val body = try {
                            call.receive<PlayerSyncRequest>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                        val uuid = try {
                            uuidFromAnyString(body.uuid)
                        } catch (e: Exception) {
                            call.application.environment.log.info("Bad Request: Invalid UUID")
                            call.respond(HttpStatusCode.BadRequest, "Invalid UUID")
                            return@post
                        }
                        call.application.environment.log.info("Syncing player: $uuid")
                        delegate.syncPlayer(uuid)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("events/ban/uuid") {
                        val ban = try {
                            call.receive<PlayerBan>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                        call.application.environment.log.info("Banning player: ${ban.bannedPlayer?.uuid}")
                        delegate.banPlayer(ban)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("events/ban/ip") {
                        val ban = try {
                            call.receive<IPBan>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                        call.application.environment.log.info("Banning ip: ${ban.ipAddress}")
                        delegate.banIP(ban)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("events/config") {
                        val config = try {
                            call.receive<RemoteConfigVersion>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                        call.application.environment.log.info("Received config version ${config.version}")
                        delegate.updateConfig(config)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("events/warps/sync") {
                        val warps = try {
                            call.receive<List<Warp>>()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
                        call.application.environment.log.info("Received warps (count: ${warps.size})")
                        delegate.syncWarps(warps)
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