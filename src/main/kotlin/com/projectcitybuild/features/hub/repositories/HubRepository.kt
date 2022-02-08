package com.projectcitybuild.features.hub.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.datetime.time.Time
import javax.inject.Inject

class HubRepository @Inject constructor(
    private val dataSource: DataSource,
    private val time: Time,
) {
    fun get(): CrossServerLocation? {
        val row = dataSource.database().getFirstRow("SELECT * FROM `hub` LIMIT 1")
        if (row != null) {
            return CrossServerLocation(
                serverName = row.get("server_name"),
                worldName = row.get("world_name"),
                x = row.get("x"),
                y = row.get("y"),
                z = row.get("z"),
                pitch = row.get("pitch"),
                yaw = row.get("yaw"),
            )
        }
        return null
    }

    fun set(location: CrossServerLocation) {
        dataSource.database().executeUpdate("DELETE FROM `hub`")
        dataSource.database().executeInsert(
            "INSERT INTO `hub` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            location.serverName,
            location.worldName,
            location.x,
            location.y,
            location.z,
            location.pitch,
            location.yaw,
            time.now(),
        )
    }
}