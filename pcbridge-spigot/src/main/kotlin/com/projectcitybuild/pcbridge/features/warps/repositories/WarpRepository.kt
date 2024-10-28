package com.projectcitybuild.pcbridge.features.warps.repositories

import com.projectcitybuild.pcbridge.http.models.Warp
import com.projectcitybuild.pcbridge.http.services.WarpHttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarpRepository(
    private val warpHttpService: WarpHttpService,
) {
    private var cache: List<Warp>? = null

    suspend fun all(): List<Warp> {
        return cache
            ?: warpHttpService.get().also { cache = it }
    }

    suspend fun get(name: String): Warp? {
        check(name.isNotEmpty())

        val expected = name.lowercase()
        return all().firstOrNull { it.name.lowercase() == expected }
    }

    suspend fun create(warp: Warp) {
        warpHttpService.create(warp)
        cache = null
    }

    suspend fun delete(name: String) {
        check(name.isNotEmpty())

        val warp = get(name)
        check (warp != null) {
            "$name warp does not exist"
        }
        warpHttpService.delete(warp)
        cache = null
    }

    suspend fun rename(
        oldName: String,
        newName: String,
    ) = withContext(Dispatchers.IO) {
        val oldWarp = get(oldName)
        checkNotNull (oldWarp) {
            "$oldName warp does not exist"
        }
        check (get(newName) == null) {
            "$newName warp already exists"
        }

        warpHttpService.update(
            oldWarp.copy(name = newName),
        )
        cache = null
    }

    suspend fun move(
        name: String,
        world: String,
        x: Double,
        y: Double,
        z: Double,
        pitch: Float,
        yaw: Float,
    ) = withContext(Dispatchers.IO) {
        val warp = get(name)
        check(warp != null) {
            "$name warp does not exist"
        }

        warpHttpService.update(
            warp.copy(
                world = world,
                x = x,
                y = y,
                z = z,
                pitch = pitch,
                yaw = yaw,
            )
        )
        cache = null
    }

    fun invalidate() {
        cache = null
    }
}
