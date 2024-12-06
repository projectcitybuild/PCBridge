package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.pcb.models.Warp
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class WarpHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getWarps()
        }
    }

    suspend fun create(warp: Warp) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().createWarp(
                name = warp.name,
                world = warp.world,
                x = warp.x,
                y = warp.y,
                z = warp.z,
                pitch = warp.pitch,
                yaw = warp.yaw,
            )
        }
    }

    suspend fun update(warp: Warp) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().updateWarp(
                id = warp.id,
                name = warp.name,
                world = warp.world,
                x = warp.x,
                y = warp.y,
                z = warp.z,
                pitch = warp.pitch,
                yaw = warp.yaw,
            )
        }
    }

    suspend fun delete(warp: Warp) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().deleteWarp(id = warp.id)
        }
    }
}
