package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.models.pcb.Build
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

    suspend fun getByName(name: String) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getBuildByName(name)
        }
    }

    suspend fun names() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getNames()
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

    // suspend fun update(warp: Warp) = withContext(Dispatchers.IO) {
    //     responseParser.parse {
    //         retrofit.pcb().updateWarp(
    //             id = warp.id,
    //             name = warp.name,
    //             world = warp.world,
    //             x = warp.x,
    //             y = warp.y,
    //             z = warp.z,
    //             pitch = warp.pitch,
    //             yaw = warp.yaw,
    //         )
    //     }
    // }
    //
    // suspend fun delete(warp: Warp) = withContext(Dispatchers.IO) {
    //     responseParser.parse {
    //         retrofit.pcb().deleteWarp(id = warp.id)
    //     }
    // }
}
