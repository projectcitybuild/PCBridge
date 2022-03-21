package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.sharedcache.SharedCacheSetFactory
import dagger.Reusable
import javax.inject.Inject

@Reusable
class WarpRepository @Inject constructor(
    private val dataSource: DataSource,
    sharedCacheSetFactory: SharedCacheSetFactory,
) {
    private val sharedCacheSet = sharedCacheSetFactory.build("all_warp_names")

    fun exists(name: String): Boolean {
        return first(name) != null
    }

    fun first(name: String): Warp? {
        return dataSource.database()
            .getFirstRow("SELECT * FROM `warps` WHERE `name`= ? LIMIT 1", name)
            ?.let { row ->
                Warp(
                    name = row.get("name"),
                    location = CrossServerLocation(
                        serverName = row.get("server_name"),
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
        val cached = sharedCacheSet.all()
        if (cached.isNotEmpty()) {
            return cached.sorted()
        }

        return dataSource.database()
            .getResults("SELECT `name` FROM `warps` ORDER BY `name` ASC")
            .map { row -> row.getString("name") }
            .also { warpNames ->
                sharedCacheSet.add(warpNames)
            }
    }

    fun all(): List<Warp> {
        return dataSource.database()
            .getResults("SELECT * FROM `warps` ORDER BY `name` ASC")
            .map { row ->
                Warp(
                    name = row.get("name"),
                    location = CrossServerLocation(
                        serverName = row.get("server_name"),
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

    fun add(warp: Warp) {
        dataSource.database().executeInsert(
            "INSERT INTO `warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            warp.name,
            warp.location.serverName,
            warp.location.worldName,
            warp.location.x,
            warp.location.y,
            warp.location.z,
            warp.location.pitch,
            warp.location.yaw,
            warp.createdAt,
        )

        sharedCacheSet.removeAll()
    }

    fun delete(name: String) {
        dataSource.database()
            .executeUpdate("DELETE FROM `warps` WHERE `name`= ?", name)

        sharedCacheSet.remove(name)
    }
}
