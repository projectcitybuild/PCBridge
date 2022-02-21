package com.projectcitybuild.features.mail.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.Mail
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class MailRepository @Inject constructor(
    private val dataSource: DataSource,
) {
    fun numberOfUncleared(playerUUID: UUID): Int {
        return dataSource.database()
            .getFirstRow(
                "SELECT COUNT(*) AS count FROM `mail` WHERE `receiver_uuid` = ? AND `is_cleared` = false",
                playerUUID.toString(),
            )
            .get("count")
    }

    fun firstUncleared(playerUUID: UUID, offset: Int): Mail? {
        return dataSource.database()
            .getResults(
                "SELECT * FROM `mail` WHERE `receiver_uuid` = ? AND `is_cleared` = false ORDER BY `created_at` ASC LIMIT ?,1",
                playerUUID.toString(),
                offset,
            )
            .map { row ->
                Mail(
                    id = row.get("id"),
                    senderUUID = UUID.fromString(row.get("sender_uuid")),
                    senderName = row.get("sender_name"),
                    receiverUUID = UUID.fromString(row.get("receiver_uuid")),
                    receiverName = row.get("receiver_name"),
                    message = row.get("message"),
                    isRead = row.get("is_read"),
                    isCleared = row.get("is_cleared"),
                    readAt = row.get("read_at"),
                    clearedAt = row.get("cleared_at"),
                    createdAt = row.get("created_at"),
                )
            }
            .firstOrNull()
    }

    fun markAsRead(id: Long) {
        dataSource.database().executeUpdate(
            "UPDATE `mail` SET `is_read` = true, `read_at` = ? WHERE `id` = ?",
            LocalDateTime.now(),
            id,
        )
    }

    fun clear(id: Long) {
        dataSource.database().executeUpdate(
            "UPDATE `mail` SET `is_cleared` = true, `cleared_at` = ? WHERE `id` = ?",
            LocalDate.now(),
            id,
        )
    }

    fun clearAll(playerUUID: UUID) {
        dataSource.database().executeUpdate(
            "UPDATE `mail` SET `is_cleared` = true, `cleared_at` = ? WHERE `receiver_uuid` = ? AND `is_cleared` = false",
            LocalDate.now(),
            playerUUID.toString(),
        )
    }

    fun send(
        senderUUID: UUID,
        senderName: String,
        targetUUID: UUID,
        targetName: String,
        message: String,
    ) {
        dataSource.database().executeInsert(
            "INSERT INTO `mail` VALUES (NULL, ?, ?, ?, ?, ?, false, false, NULL, NULL, ?)",
            senderUUID.toString(),
            senderName,
            targetUUID.toString(),
            targetName,
            message,
            LocalDateTime.now(),
        )
    }
}
