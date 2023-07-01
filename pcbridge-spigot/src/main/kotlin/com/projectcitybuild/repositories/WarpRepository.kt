package com.projectcitybuild.repositories

import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.database.DataSource

class WarpRepository(
    private val dataSource: DataSource,
) {
    private val nameCache: MutableList<String> = mutableListOf()
    private var hasBuiltNameCache = false

    fun exists(name: String): Boolean {
        return first(name) != null
    }

    fun first(name: String): Warp? {
        return dataSource.database()
            .getFirstRow("SELECT * FROM `warps` WHERE `name`= ? LIMIT 1", name)
            ?.let { row ->
                Warp(
                    name = row.get("name"),
                    location = SerializableLocation(
                        worldName = row.get("world_name"),
                        x = row.get("x"),
                        y = row.get("y"),
                        z = row.get("z"),
                        pitch = row.get("pitch"),
                        yaw = row.get("yaw"),
                    ),
                    createdAt = row.get("created_at"),
                )
            }
    }

    fun names(): List<String> {
        if (hasBuiltNameCache) {
            return nameCache
        }
        return dataSource.database()
            .getResults("SELECT `name` FROM `warps` ORDER BY `name` ASC")
            .map { row -> row.getString("name") }
            .also { warpNames ->
                nameCache.addAll(warpNames)
                nameCache.sort()
                hasBuiltNameCache = true
            }
    }

    fun all(): List<Warp> {
        return dataSource.database()
            .getResults("SELECT * FROM `warps` ORDER BY `name` ASC")
            .map { row ->
                Warp(
                    name = row.get("name"),
                    location = SerializableLocation(
                        worldName = row.get("world_name"),
                        x = row.get("x"),
                        y = row.get("y"),
                        z = row.get("z"),
                        pitch = row.get("pitch"),
                        yaw = row.get("yaw"),
                    ),
                    createdAt = row.get("created_at"),
                )
            }
            .also {
                nameCache.clear()
                nameCache.addAll(it.map { warp -> warp.name })
                nameCache.sort()
                hasBuiltNameCache = true
            }
    }

    fun add(warp: Warp) {
        dataSource.database().executeInsert(
            "INSERT INTO `warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            warp.name,
            warp.location.worldName,
            warp.location.x,
            warp.location.y,
            warp.location.z,
            warp.location.pitch,
            warp.location.yaw,
            warp.createdAt,
        )

        nameCache.clear()
        hasBuiltNameCache = false
    }

    fun delete(name: String) {
        dataSource.database()
            .executeUpdate("DELETE FROM `warps` WHERE `name`= ?", name)

        nameCache.remove(name)
    }

    fun flush() {
        nameCache.clear()
        hasBuiltNameCache = false
    }
}
