package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class WarpHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun all(page: Int, size: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getWarps(page, size)
        }
    }

    suspend fun all() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getAllWarps()
        }
    }

    suspend fun get(id: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getWarp(id)
        }
    }

    suspend fun create(
        name: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        pitch: Float,
        yaw: Float,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().createWarp(
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
        name: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        pitch: Float,
        yaw: Float,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().updateWarp(
                id = id,
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

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().deleteWarp(id = id)
        }
    }

    suspend fun names() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getWarpNames()
        }
    }
}
