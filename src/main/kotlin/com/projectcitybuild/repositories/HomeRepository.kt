package com.projectcitybuild.repositories

import co.aikar.idb.DbRow
import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Home
import dagger.Reusable
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

@Reusable
class HomeRepository @Inject constructor(
    private val dataSource: DataSource,
//    sharedCacheSetFactory: SharedCacheSetFactory,
) {
//    private val sharedCacheSet = sharedCacheSetFactory.build("all_warp_names")

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
//        val cached = sharedCacheSet.all()
//        if (cached.isNotEmpty()) {
//            return cached.sorted()
//        }

        return dataSource.database()
            .getResults(
                "SELECT `name` FROM `homes` WHERE `player_uuid` = ? ORDER BY `name` ASC",
                playerUUID.toString()
            )
            .map { row -> row.getString("name") }
//            .also { warpNames ->
//                sharedCacheSet.add(warpNames)
//            }
    }

    fun all(playerUUID: UUID): List<Home> {
        return dataSource.database()
            .getResults(
                "SELECT * FROM `homes` WHERE `player_uuid` = ? ORDER BY `name` ASC",
                playerUUID.toString()
            )
            .map { row -> Home.fromDBRow(row) }
    }

    fun add(home: Home) {
        dataSource.database().executeInsert(
            "INSERT INTO `homes` VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            home.name,
            home.playerUUID,
            home.location.serverName,
            home.location.worldName,
            home.location.x,
            home.location.y,
            home.location.z,
            home.location.pitch,
            home.location.yaw,
            home.createdAt,
        )

//        sharedCacheSet.removeAll()
    }

    fun delete(name: String, playerUUID: UUID) {
        dataSource.database()
            .executeUpdate(
                "DELETE FROM `homes` WHERE `name`= ? AND `player_uuid` = ?",
                name,
                playerUUID.toString(),
            )

//        sharedCacheSet.remove(name)
    }
}

private fun Home.Companion.fromDBRow(row: DbRow): Home {
    return Home(
        id = row.get("id"),
        name = row.get("name"),
        playerUUID = java.util.UUID.fromString("player_uuid"),
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