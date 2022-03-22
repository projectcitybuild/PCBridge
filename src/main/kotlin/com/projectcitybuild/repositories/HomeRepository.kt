package com.projectcitybuild.repositories

import co.aikar.idb.DbRow
import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Home
import com.projectcitybuild.modules.sharedcache.SharedCacheSetFactory
import dagger.Reusable
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@Reusable
class HomeRepository @Inject constructor(
    private val dataSource: DataSource,
    sharedCacheSetFactory: SharedCacheSetFactory,
) {
    private val sharedCacheSet = sharedCacheSetFactory.build("home_names")

    fun exists(name: String, playerUUID: UUID): Boolean {
        return first(name, playerUUID) != null
    }

    fun first(name: String, playerUUID: UUID): Home? {
        return dataSource.database()
            .getFirstRow(
                "SELECT * FROM `homes` WHERE `name`= ? AND `player_uuid` = ? LIMIT 1",
                name,
                playerUUID.toString()
            )
            ?.let { row -> Home.fromDBRow(row) }
    }

    fun names(playerUUID: UUID): List<String> {
        val cached = sharedCacheSet.all(subKey = playerUUID.toString())
        if (cached.isNotEmpty()) {
            return cached.sorted()
        }

        return dataSource.database()
            .getResults(
                "SELECT `name` FROM `homes` WHERE `player_uuid` = ? ORDER BY `name` ASC",
                playerUUID.toString()
            )
            .map { row -> row.getString("name") }
            .also { homeNames ->
                sharedCacheSet.add(
                    values = homeNames,
                    subKey = playerUUID.toString(),
                )
            }
            .sorted()
    }

    fun all(playerUUID: UUID): List<Home> {
        return dataSource.database()
            .getResults(
                "SELECT * FROM `homes` WHERE `player_uuid` = ? ORDER BY `name` ASC",
                playerUUID.toString()
            )
            .map { row -> Home.fromDBRow(row) }
    }

    fun add(
        playerUUID: UUID,
        homeName: String,
        location: CrossServerLocation,
        createdAt: LocalDateTime
    ) {
        dataSource.database().executeInsert(
            "INSERT INTO `homes` VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            homeName,
            playerUUID.toString(),
            location.serverName,
            location.worldName,
            location.x,
            location.y,
            location.z,
            location.pitch,
            location.yaw,
            createdAt,
        )

        sharedCacheSet.removeAll(subKey = playerUUID.toString())
    }

    fun delete(name: String, playerUUID: UUID) {
        dataSource.database()
            .executeUpdate(
                "DELETE FROM `homes` WHERE `name`= ? AND `player_uuid` = ?",
                name,
                playerUUID.toString(),
            )

        sharedCacheSet.remove(
            value = name,
            subKey = playerUUID.toString(),
        )
    }
}

private fun Home.Companion.fromDBRow(row: DbRow): Home {
    return Home(
        id = row.get("id"),
        name = row.get("name"),
        playerUUID = UUID.fromString(row.get("player_uuid")),
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