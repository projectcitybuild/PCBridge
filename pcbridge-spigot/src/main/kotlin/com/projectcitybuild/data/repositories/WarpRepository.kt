package com.projectcitybuild.data.repositories

import co.aikar.idb.DbRow
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.core.database.DatabaseSession
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarpRepository(
    private val db: DatabaseSession,
    private val cache: Cache<String, Warp>,
) {
    suspend fun all(): List<Warp> = withContext(Dispatchers.IO) {
        val cached = cache.asMap()
        if (cached.isNotEmpty()) {
            return@withContext cached.values.asIterable().toList()
        }
        db.database()
            ?.getResults("SELECT * FROM `warps` ORDER BY `name` ASC")
            ?.map { it.toWarp() }
            ?.also { warps ->
                cache.invalidateAll()
                warps.forEach { warp -> cache.put(warp.name, warp) }
            }
            ?: emptyList()
    }
}

private fun DbRow.toWarp() = Warp(
    name = get("name"),
    location = SerializableLocation(
        worldName = get("world_name"),
        x = get("x"),
        y = get("y"),
        z = get("z"),
        pitch = get("pitch"),
        yaw = get("yaw"),
    ),
    createdAt = get("created_at"),
)