package com.projectcitybuild.features.teleporthistory.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.LastKnownLocation
import com.projectcitybuild.modules.database.DataSource
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class LastKnownLocationRepositoy @Inject constructor(
    private val dataSource: DataSource,
) {
    fun set(
        playerUUID: UUID,
        location: CrossServerLocation,
    ) {
        val lastKnownLocation = get(playerUUID)
        if (lastKnownLocation == null) {
            dataSource.database().executeInsert(
                "INSERT INTO last_known_locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                playerUUID.toString(),
                location.serverName,
                location.worldName,
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw,
                LocalDateTime.now(),
            )
        } else {
            dataSource.database().executeUpdate(
                "UPDATE last_known_locations SET server_name = ?, world_name = ?, x = ?, y = ?, z = ?, pitch = ?, yaw = ?, created_at = ? WHERE player_uuid = ?",
                playerUUID.toString(),
                location.serverName,
                location.worldName,
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw,
                LocalDateTime.now(),
            )
        }
    }

    fun get(playerUUID: UUID): List<LastKnownLocation> {
        return dataSource.database().getResults(
            "SELECT * FROM last_known_locations WHERE player_uuid = ? LIMIT 1",
            playerUUID.toString()
        ).map {
            LastKnownLocation(
                playerUUID = UUID.fromString(it.get("player_uuid")),
                location = CrossServerLocation(
                    serverName = it.get("server_name"),
                    worldName = it.get("world_name"),
                    x = it.get("x"),
                    y = it.get("y"),
                    z = it.get("z"),
                    pitch = it.get("pitch"),
                    yaw = it.get("yaw"),
                ),
                createdAt = LocalDateTime.now(),
            )
        }
    }
}