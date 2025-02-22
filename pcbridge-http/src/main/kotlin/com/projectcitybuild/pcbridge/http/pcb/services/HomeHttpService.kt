package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class HomeHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun all(playerUUID: UUID, page: Int, size: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getHomes(playerUUID.toString(), page, size)
        }
    }

    suspend fun create(
        playerUUID: UUID,
        name: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        pitch: Float,
        yaw: Float,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().createHome(
                playerUUID = playerUUID.toString(),
                name = name,
                world = world,
                x = x,
                y = y,
                z = z,
                pitch = pitch,
                yaw = yaw,
            )
        }
    }

    suspend fun delete(
        playerUUID: UUID,
        id: Int,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().deleteHome(
                playerUUID = playerUUID.toString(),
                id = id,
            )
        }
    }
}
