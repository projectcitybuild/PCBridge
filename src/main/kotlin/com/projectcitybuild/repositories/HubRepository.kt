package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.modules.datetime.time.Time
import javax.inject.Inject

class HubRepository @Inject constructor(
    private val dataSource: DataSource,
    private val time: Time,
) {
    fun get(): SerializableLocation? {
        val row = dataSource.database().getFirstRow("SELECT * FROM `hub` LIMIT 1")
        if (row != null) {
            return SerializableLocation(
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

    fun set(location: SerializableLocation) {
        dataSource.database().executeUpdate("DELETE FROM `hub`")
        dataSource.database().executeInsert(
            "INSERT INTO `hub` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            "", // TODO: remove 'server name' parameter via migration
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
