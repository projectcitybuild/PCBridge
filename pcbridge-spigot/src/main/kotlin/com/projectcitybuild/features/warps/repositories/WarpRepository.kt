package com.projectcitybuild.features.warps.repositories

import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.core.pagination.Page
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import kotlin.math.ceil

class WarpRepository(
    private val db: DatabaseSession,
    private val cache: Cache<String, Warp>,
) {
    suspend fun all(
        limit: Int,
        page: Int = 1,
    ): Page<Warp> = withContext(Dispatchers.IO) {
        check (page >= 1) { "Page must be greater than 0" }

        val startIndex = (page - 1) * limit
        val warpCount = db.connect { connection ->
            connection.prepareStatement("SELECT COUNT(*) AS `count` FROM `warps`")
                .executeQuery()
                .firstRow()
                .getLong("count")
        }
        val warps = db.connect { connection ->
            connection.prepareStatement("SELECT * FROM `warps` ORDER BY `name` ASC LIMIT ? OFFSET ?",)
                .apply {
                    setInt(1, limit)
                    setInt(2, startIndex)
                }
                .executeQuery()
                .mapRows { it.toWarp() }
        }
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
        val oldExists = db.connect { connection ->
            connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                .apply { setString(1, oldName) }
                .executeQuery()
                .isBeforeFirst
        }
        check (oldExists) {
            "$oldName warp does not exist"
        }

        val newExists = db.connect { connection ->
            connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                .apply { setString(1, newName) }
                .executeQuery()
                .isBeforeFirst
        }
        check (!newExists) {
            "$newName warp already exists"
        }

        val success = db.connect { connection ->
            connection.prepareStatement("UPDATE `warps` SET `name`=? WHERE `name`= ?")
                .apply {
                    setString(1, newName)
                    setString(2, oldName)
                }
                .executeUpdate() == 1
        }
        check (success) {
            "Database write operation failed: no affected rows"
        }
    }
}

private fun ResultSet.firstRow(): ResultSet {
    next()
    return this
}

private fun <T> ResultSet.mapRows(transform: (ResultSet) -> T): List<T> {
    val rows = mutableListOf<T>()
    while (next()) {
        rows.add(transform(this))
    }
    return rows
}

private fun ResultSet.toWarp() = Warp(
    name = getString("name"),
    location = SerializableLocation(
        worldName = getString("world_name"),
        x = getDouble("x"),
        y = getDouble("y"),
        z = getDouble("z"),
        pitch = getFloat("pitch"),
        yaw = getFloat("yaw"),
    ),
    createdAt = getTimestamp("created_at").toLocalDateTime(),
)