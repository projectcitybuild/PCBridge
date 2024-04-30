package com.projectcitybuild.data.repositories

import co.aikar.idb.DB.getResults
import co.aikar.idb.DbRow
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.core.pagination.Page
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class WarpRepository(
    private val db: DatabaseSession,
    private val cache: Cache<String, Warp>,
) {
    suspend fun all(
        limit: Int,
        page: Int = 0,
    ): Page<Warp> = withContext(Dispatchers.IO) {
        check(page >= 0) { "Page cannot be negative" }
        // val cached = cache.asMap()
        // if (cached.isNotEmpty()) {
        //     val start = offset * limit
        //     val end = start + limit
        //
        //     return@withContext cached.values
        //         .asIterable()
        //         .sortedByDescending { true }
        //         .toList()
        //         .slice(start..end)
        // }

        val startIndex = page * limit

        val warpCount = db.database()
            ?.getResults("SELECT COUNT(*) FROM `warps` AS `count`")
            ?.firstOrNull()
            ?.get("column")
            ?: 0

        val warps = db.database()
            ?.getResults("SELECT * FROM `warps` ORDER BY `name` ASC LIMIT ? OFFSET ?", limit, startIndex)
            ?.map { it.toWarp() }
            ?.also { warps ->
                cache.invalidateAll()
                warps.forEach { warp -> cache.put(warp.name, warp) }
            }
            ?: emptyList()

        return@withContext Page(
            items = warps,
            page = page,
            totalPages = ceil(warpCount.toDouble() / limit.toDouble()).toInt(),
        )
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