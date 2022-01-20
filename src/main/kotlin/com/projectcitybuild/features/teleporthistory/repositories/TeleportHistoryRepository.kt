package com.projectcitybuild.features.teleporthistory.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.TeleportReason
import com.projectcitybuild.entities.TeleportRecord
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.logger.PlatformLogger
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class TeleportHistoryRepository @Inject constructor(
    private val dataSource: DataSource,
    private val logger: PlatformLogger
) {
    private val MAX_ACTIVE_RECORDS = 3

    fun add(playerUUID: UUID, location: CrossServerLocation, reason: TeleportReason) {
        val teleportHistory = get(playerUUID)

        val sortedActiveRecords = teleportHistory.filter { it.canGoBack }

        // Delete active records until it's no-longer over the max capacity
        val newTotal = sortedActiveRecords.size + 1
        if (newTotal >= MAX_ACTIVE_RECORDS) {
            val difference = newTotal - MAX_ACTIVE_RECORDS
            logger.debug("Purging $difference teleport records due to over-capacity")

            val newestIdToDelete = sortedActiveRecords[difference - 1].id
            dataSource.database().executeUpdate(
                "DELETE FROM teleport_history WHERE player_uuid = ? AND id <= ?",
                playerUUID.toString(),
                newestIdToDelete,
            )
        }

        // Delete records in-front
        dataSource.database().executeUpdate(
            "DELETE FROM teleport_history WHERE player_uuid = ? AND can_go_back = false",
            playerUUID.toString(),
        )
        // Insert new record
        dataSource.database().executeInsert(
            "INSERT INTO teleport_history VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            playerUUID.toString(),
            reason.toString(),
            location.serverName,
            location.worldName,
            location.x,
            location.y,
            location.z,
            location.pitch,
            location.yaw,
            true,
            LocalDateTime.now(),
        )
    }

    fun get(playerUUID: UUID): List<TeleportRecord> {
        return dataSource.database().getResults(
            "SELECT * FROM teleport_history WHERE player_uuid = ? ORDER BY created_at ASC",
            playerUUID.toString()
        ).map {
            TeleportRecord(
                id = it.get("id"),
                playerUUID = UUID.fromString(it.get("player_uuid")),
                teleportReason = when(it.getString("tp_reason")) {
                    "TP_SUMMON" -> TeleportReason.TP_SUMMON
                    "TP_PLAYER" -> TeleportReason.TP_PLAYER
                    "WARP" -> TeleportReason.WARP
                    else -> TeleportReason.TP_PLAYER
                },
                location = CrossServerLocation(
                    serverName = it.get("server_name"),
                    worldName = it.get("world_name"),
                    x = it.get("x"),
                    y = it.get("y"),
                    z = it.get("z"),
                    pitch = it.get("pitch"),
                    yaw = it.get("yaw"),
                ),
                canGoBack = it.get("can_go_back"),
                createdAt = LocalDateTime.now(),
            )
        }
    }
}