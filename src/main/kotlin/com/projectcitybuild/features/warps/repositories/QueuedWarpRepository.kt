package com.projectcitybuild.features.warps.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.core.infrastructure.database.DataSource
import java.util.*
import javax.inject.Inject

class QueuedWarpRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun queue(playerUUID: UUID, warp: Warp) {
        if (get(playerUUID) != null) {
            dequeue(playerUUID)
        }
        dataSource.database().executeInsert(
            "INSERT INTO `queued_warps` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            playerUUID.toString(),
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
    }

    fun dequeue(playerUUID: UUID) {
        dataSource.database().executeUpdate(
            "DELETE FROM `queued_warps` WHERE `player_uuid` = ?",
            playerUUID.toString()
        )
    }

    fun get(playerUUID: UUID): Warp? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM `queued_warps` WHERE `player_uuid` = ? LIMIT 1",
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