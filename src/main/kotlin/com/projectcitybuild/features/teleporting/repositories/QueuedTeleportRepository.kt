package com.projectcitybuild.features.teleporting.repositories

import com.projectcitybuild.entities.Teleport
import com.projectcitybuild.modules.database.DataSource
import java.util.*
import javax.inject.Inject

class QueuedTeleportRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun queue(teleport: Teleport) {
        if (get(teleport.playerUUID) != null) {
            dequeue(teleport.playerUUID)
        }
        dataSource.database().executeInsert(
            "INSERT INTO `queued_teleports` VALUES (?, ?, ?, ?)",
            teleport.playerUUID.toString(),
            teleport.targetPlayerUUID.toString(),
            teleport.targetServerName,
            teleport.createdAt,
        )
    }

    fun dequeue(playerUUID: UUID) {
        dataSource.database().executeUpdate(
            "DELETE FROM `queued_teleports` WHERE `player_uuid` = ?",
            playerUUID.toString()
        )
    }

    fun get(playerUUID: UUID): Teleport? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM `queued_teleports` WHERE `player_uuid` = ? LIMIT 1",
            playerUUID.toString()
        )
        if (row != null) {
            return Teleport(
                playerUUID = UUID.fromString(row.get("player_uuid")),
                targetPlayerUUID = UUID.fromString(row.get("target_player_uuid")),
                targetServerName = row.get("target_server_name"),
                createdAt = row.get("created_at"),
            )
        }
        return null
    }
}