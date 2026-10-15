package com.projectcitybuild.pcbridge.runtime.connection.middleware

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Authorization
import java.util.UUID

interface ConnectionMiddleware {
    suspend fun handle(
        uuid: UUID,
        ip: java.net.InetAddress,
        authorization: Authorization,
    ): ConnectionResult
}
