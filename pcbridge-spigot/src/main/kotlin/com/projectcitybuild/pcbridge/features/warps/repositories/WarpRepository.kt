package com.projectcitybuild.pcbridge.features.warps.repositories

import com.projectcitybuild.pcbridge.core.database.DatabaseSession
import com.projectcitybuild.pcbridge.data.Page
import com.projectcitybuild.pcbridge.data.SerializableLocation
import com.projectcitybuild.pcbridge.features.warps.Warp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Date.valueOf
import java.sql.ResultSet
import kotlin.math.ceil
import kotlin.math.min

class WarpRepository(
    private val db: DatabaseSession,
) {
    private var cache: List<Warp>? = null

    private suspend fun cachedOrFetchAll(): List<Warp> {
        return cache
            ?: withContext(Dispatchers.IO) {
                db.connect { connection ->
                    connection.prepareStatement("SELECT * FROM `warps` ORDER BY `name` ASC")
                        .use { it.executeQuery() }
                        .use { it.mapRows { row -> row.toWarp() } }
                }
            }
            .also { cache = it }
    }

    suspend fun all(
        limit: Int,
        page: Int = 1,
    ): Page<Warp> {
        check(page >= 1) { "Page must be greater than 0" }

        val warps = cachedOrFetchAll()

        val startIndex = (page - 1) * limit
        val lower = min(warps.size, startIndex) // inclusive
        val upper = min(warps.size, startIndex + limit) // exclusive
        val totalPages = ceil(warps.size.toDouble() / limit.toDouble()).toInt()

        return Page(
            items = if (lower < upper) warps.subList(lower, upper) else emptyList(),
            page = page,
            totalPages = totalPages,
        )
    }

    suspend fun get(name: String): Warp? {
        check(name.isNotEmpty())

        return cachedOrFetchAll()
            .firstOrNull { it.name.lowercase() == name.lowercase() }
    }

    suspend fun create(warp: Warp) =
        withContext(Dispatchers.IO) {
            val success =
                db.connect { connection ->
                    connection.prepareStatement("INSERT INTO `warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                        .apply {
                            setString(1, warp.name)
                            setString(2, warp.location.worldName)
                            setDouble(3, warp.location.x)
                            setDouble(4, warp.location.y)
                            setDouble(5, warp.location.z)
                            setFloat(6, warp.location.pitch)
                            setFloat(7, warp.location.yaw)
                            setDate(8, valueOf(warp.createdAt.toLocalDate()))
                        }
                        .use { it.executeUpdate() } == 1
                }
            check(success) {
                "Database write operation failed"
            }
            cache = null
        }

    suspend fun delete(name: String) =
        withContext(Dispatchers.IO) {
            val exists =
                db.connect { connection ->
                    connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                        .apply { setString(1, name) }
                        .use { it.executeQuery() }
                        .use { it.isBeforeFirst }
                }
            check(exists) {
                "$name warp does not exist"
            }

            val success =
                db.connect { connection ->
                    connection.prepareStatement("DELETE `warps` WHERE `name`= ?")
                        .apply { setString(1, name) }
                        .executeUpdate() == 1
                }
            check(success) {
                "Database write operation failed: no affected rows"
            }
            cache = null
        }

    suspend fun rename(
        oldName: String,
        newName: String,
    ) = withContext(Dispatchers.IO) {
        val oldExists =
            db.connect { connection ->
                connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                    .apply { setString(1, oldName) }
                    .use { it.executeQuery() }
                    .use { it.isBeforeFirst }
            }
        check(oldExists) {
            "$oldName warp does not exist"
        }

        val newExists =
            db.connect { connection ->
                connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                    .apply { setString(1, newName) }
                    .use { it.executeQuery() }
                    .use { it.isBeforeFirst }
            }
        check(!newExists) {
            "$newName warp already exists"
        }

        val success =
            db.connect { connection ->
                connection.prepareStatement("UPDATE `warps` SET `name`=? WHERE `name`= ?")
                    .apply {
                        setString(1, newName)
                        setString(2, oldName)
                    }
                    .use { it.executeUpdate() } == 1
            }
        check(success) {
            "Database write operation failed: no affected rows"
        }
        cache = null
    }

    suspend fun move(
        name: String,
        newLocation: SerializableLocation,
    ) = withContext(Dispatchers.IO) {
        val exists =
            db.connect { connection ->
                connection.prepareStatement("SELECT * FROM `warps` WHERE `name`=?")
                    .apply { setString(1, name) }
                    .executeQuery()
                    .isBeforeFirst
            }
        check(exists) {
            "$name warp does not exist"
        }

        val success =
            db.connect { connection ->
                connection.prepareStatement("UPDATE `warps` SET `world`=?, `x`=?, `y`=?, `z`=?, `pitch`=?, `yaw`=? WHERE `name`= ?")
                    .apply {
                        setString(1, newLocation.worldName)
                        setDouble(2, newLocation.x)
                        setDouble(3, newLocation.y)
                        setDouble(4, newLocation.z)
                        setFloat(5, newLocation.yaw)
                        setFloat(6, newLocation.pitch)
                        setString(7, name)
                    }
                    .use { it.executeUpdate() } == 1
            }
        check(success) {
            "Database write operation failed: no affected rows"
        }
        cache = null
    }
}

private fun <T> ResultSet.mapRows(transform: (ResultSet) -> T): List<T> {
    val rows = mutableListOf<T>()
    while (next()) {
        rows.add(transform(this))
    }
    return rows
}

private fun ResultSet.toWarp() =
    Warp(
        name = getString("name"),
        location =
            SerializableLocation(
                worldName = getString("world_name"),
                x = getDouble("x"),
                y = getDouble("y"),
                z = getDouble("z"),
                pitch = getFloat("pitch"),
                yaw = getFloat("yaw"),
            ),
        createdAt = getTimestamp("created_at").toLocalDateTime(),
    )
