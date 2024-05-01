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
        page: Int = 1,
    ): Page<Warp> = withContext(Dispatchers.IO) {
        check(page >= 1) { "Page must be greater than 0" }
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

        val startIndex = (page - 1) * limit

        val warpCount = db.database()
            ?.getResults("SELECT COUNT(*) AS `count` FROM `warps`")
            ?.firstOrNull()
            ?.getLong("count")
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

    suspend fun rename(
        oldName: String,
        newName: String,
    ) = withContext(Dispatchers.IO) {
        val oldExists = db.database()
            ?.getResults("SELECT * FROM `warps` WHERE `name`=?", oldName)
            ?.isNotEmpty()
            ?: false

        check(oldExists) {
            "$oldName warp does not exist"
        }

        val newExists = db.database()
            ?.getResults("SELECT * FROM `warps` WHERE `name`=?", newName)
            ?.isNotEmpty()
            ?: false

        check(!newExists) {
            "$newName warp already exists"
        }

        val success = db.database()
            ?.executeUpdate("UPDATE `warps` SET `name`=? WHERE `name`= ?", newName, oldName)
            ?: 0

        check(success == 1) {
            "Database write operation failed: no affected rows"
        }
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