package com.projectcitybuild.modules.teleport.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class QueuedLocationTeleportRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun queue(playerUUID: UUID, destinationName: String, destination: CrossServerLocation) {
        if (get(playerUUID) != null) {
            dequeue(playerUUID)
        }
        dataSource.database().executeInsert(
            "INSERT INTO `queued_location_teleports` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            playerUUID.toString(),
            destinationName,
            destination.serverName,
            destination.worldName,
            destination.x,
            destination.y,
            destination.z,
            destination.pitch,
            destination.yaw,
            LocalDateTime.now(),
        )
    }

    fun dequeue(playerUUID: UUID) {
        dataSource.database().executeUpdate(
            "DELETE FROM `queued_location_teleports` WHERE `player_uuid` = ?",
            playerUUID.toString()
        )
    }

    fun get(playerUUID: UUID): Warp? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM `queued_location_teleports` WHERE `player_uuid` = ? LIMIT 1",
            playerUUID.toString()
        )
        if (row != null) {
            return Warp(
                name = row.get("warp_name"),
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
        return null
    }
}