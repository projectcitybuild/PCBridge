package com.projectcitybuild.features.teleporting.repositories

import com.projectcitybuild.entities.QueuedTeleport
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.modules.database.DataSource
import java.util.*
import javax.inject.Inject

class QueuedTeleportRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun queue(queuedTeleport: QueuedTeleport) {
        if (get(queuedTeleport.playerUUID) != null) {
            dequeue(queuedTeleport.playerUUID)
        }
        dataSource.database().executeInsert(
            "INSERT INTO `queued_teleports` VALUES (?, ?, ?, ?, ?, ?)",
            queuedTeleport.playerUUID.toString(),
            queuedTeleport.targetPlayerUUID.toString(),
            queuedTeleport.targetServerName,
            queuedTeleport.teleportType.toString(),
            if (queuedTeleport.isSilentTeleport) 1 else 0,
            queuedTeleport.createdAt,
        )
    }

    fun dequeue(playerUUID: UUID) {
        dataSource.database().executeUpdate(
            "DELETE FROM `queued_teleports` WHERE `player_uuid` = ?",
            playerUUID.toString()
        )
    }

    fun get(playerUUID: UUID): QueuedTeleport? {
        val row = dataSource.database().getFirstRow(
            "SELECT * FROM `queued_teleports` WHERE `player_uuid` = ? LIMIT 1",
            playerUUID.toString()
        )
        if (row != null) {
            return QueuedTeleport(
                playerUUID = UUID.fromString(row.get("player_uuid")),
                targetPlayerUUID = UUID.fromString(row.get("target_player_uuid")),
                targetServerName = row.get("target_server_name"),
                teleportType = when (row.getString("teleport_type")) {
                    "TP" -> TeleportType.TP
                    "SUMMON" -> TeleportType.SUMMON
                    else -> throw Exception("Unhandled TeleportType: ${row.getString("teleport_type")}")
                },
                isSilentTeleport = row.getInt("is_silent_tp") == 1,
                createdAt = row.get("created_at"),
            )
        }
        return null
    }
}