package com.projectcitybuild.pcbridge.paper.architecture.connection.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import java.util.UUID

class ConnectionMiddlewareChain {
    private val middlewares = mutableSetOf<ConnectionMiddleware>()

    fun register(vararg middleware: ConnectionMiddleware) = middleware.forEach {
        middlewares.add(it)
    }

    suspend fun run(
        uuid: UUID,
        ip: java.net.InetAddress,
        playerData: PlayerData,
    ): ConnectionResult {
        for (middleware in middlewares) {
            val result = middleware.handle(uuid, ip, playerData)
            if (result is ConnectionResult.Denied) {
                return result
            }
        }
        return ConnectionResult.Allowed
    }
}