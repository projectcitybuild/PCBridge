package com.projectcitybuild.features.warps.repositories

import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.database.DataSource
import dagger.Reusable
import javax.inject.Inject

@Reusable
class WarpRepository @Inject constructor(
    private val dataSource: DataSource,
) {
    private var cache: List<Warp>? = null

    fun exists(name: String): Boolean {
        return first(name) != null
    }

    fun first(name: String): Warp? {
        val cache = cache
        if (cache != null) {
            return cache.firstOrNull { it.name == name }
        }
        return dataSource.database()
            .getFirstRow("SELECT * FROM `warps` WHERE `name`= ? LIMIT 1", name)
            ?.let { row ->
                Warp(
                    name = row.get("name"),
                    serverName = row.get("server_name"),
                    worldName = row.get("world_name"),
                    x = row.get("x"),
                    y = row.get("y"),
                    z = row.get("z"),
                    pitch = row.get("pitch"),
                    yaw = row.get("yaw"),
                    createdAt = row.get("created_at"),
                )
            }
    }

    fun all(): List<Warp> {
        val cache = cache
        if (cache != null) {
            return cache.sortedBy { it.name }
        }

        return dataSource.database()
            .getResults("SELECT * FROM `warps` ORDER BY `name` ASC")
            .map { row ->
                Warp(
                    name = row.get("name"),
                    serverName = row.get("server_name"),
                    worldName = row.get("world_name"),
                    x = row.get("x"),
                    y = row.get("y"),
                    z = row.get("z"),
                    pitch = row.get("pitch"),
                    yaw = row.get("yaw"),
                    createdAt = row.get("created_at"),
                )
            }
            .also { this.cache = it }
    }

    fun add(warp: Warp) {
        cache = null

        dataSource.database().executeInsert(
            "INSERT INTO `warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            warp.name,
            warp.serverName,
            warp.worldName,
            warp.x,
            warp.y,
            warp.z,
            warp.pitch,
            warp.yaw,
            warp.createdAt,
        )
    }

    fun delete(name: String) {
        cache = null

        dataSource.database()
            .executeUpdate("DELETE FROM `warps` WHERE `name`= ?", name)
    }
}