package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class BuildHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun all(page: Int, size: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getBuilds(page, size)
        }
    }

    suspend fun get(id: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getBuild(id)
        }
    }

    suspend fun names() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getBuildNames()
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
            retrofit.pcb().createBuild(
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

    suspend fun update(
        id: Int,
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
            retrofit.pcb().updateBuild(
                id = id,
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

    suspend fun delete(id: Int, playerUUID: UUID) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().deleteBuild(
                id = id,
                playerUUID = playerUUID.toString(),
            )
        }
    }

    suspend fun vote(
        id: Int,
        playerUUID: UUID,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().buildVote(
                id = id,
                playerUUID = playerUUID.toString(),
            )
        }
    }
}
