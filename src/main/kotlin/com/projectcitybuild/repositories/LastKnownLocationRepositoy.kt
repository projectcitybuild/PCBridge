package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.LastKnownLocation
import com.projectcitybuild.entities.SerializableLocation
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class LastKnownLocationRepositoy @Inject constructor(
    private val dataSource: DataSource,
) {
    fun set(
        playerUUID: UUID,
        location: SerializableLocation,
    ) {
        val lastKnownLocation = get(playerUUID)
        if (lastKnownLocation == null) {
            dataSource.database().executeInsert(
                "INSERT INTO last_known_locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                playerUUID.toString(),
                "", // TODO: remove 'server name' parameter via migration
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
                "UPDATE last_known_locations SET world_name = ?, x = ?, y = ?, z = ?, pitch = ?, yaw = ?, created_at = ? WHERE player_uuid = ?",
                location.worldName,
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw,
                LocalDateTime.now(),
                playerUUID.toString(),
            )
        }
    }

    fun get(playerUUID: UUID): LastKnownLocation? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM last_known_locations WHERE player_uuid = ? LIMIT 1",
            playerUUID.toString()
        ) ?: return null

        return LastKnownLocation(
            playerUUID = UUID.fromString(row.get("player_uuid")),
            location = SerializableLocation(
                worldName = row.get("world_name"),
                x = row.get("x"),
                y = row.get("y"),
                z = row.get("z"),
                pitch = row.get("pitch"),
                yaw = row.get("yaw"),
            ),
            createdAt = LocalDateTime.now(),
        )
    }
}
