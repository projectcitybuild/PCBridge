package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class BuildHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun all(page: Int, size: Int) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getBuilds(page, size)
        }
    }

    suspend fun names() = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().getNames()
        }
    }

    // suspend fun create(warp: Warp) = withContext(Dispatchers.IO) {
    //     responseParser.parse {
    //         retrofit.pcb().createWarp(
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
