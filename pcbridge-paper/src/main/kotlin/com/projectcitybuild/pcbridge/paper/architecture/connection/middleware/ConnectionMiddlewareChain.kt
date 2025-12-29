package com.projectcitybuild.pcbridge.paper.architecture.connection.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.Authorization
import java.util.UUID

class ConnectionMiddlewareChain(
    private val middlewares: MutableList<ConnectionMiddleware> = mutableListOf(),
) {
    fun register(vararg middleware: ConnectionMiddleware) = middleware.forEach {
        middlewares.add(it)
    }

    suspend fun pipe(
        uuid: UUID,
        ip: java.net.InetAddress,
        authorization: Authorization,
    ): ConnectionResult {
        for (middleware in middlewares) {
            val result = middleware.handle(uuid, ip, authorization)
            if (result is ConnectionResult.Denied) {
                return result
            }
        }
        return ConnectionResult.Allowed
    }
}