package com.projectcitybuild.features.mail.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.Mail
import java.util.*
import javax.inject.Inject

class MailRepository @Inject constructor(
    private val dataSource: DataSource,
) {
    fun numberOfUnread(playerUUID: UUID): Int {
        return dataSource.database()
            .getFirstRow(
                "SELECT COUNT(*) AS count FROM `mail` WHERE `receiver_uuid` = ? AND `is_read` = false",
                playerUUID.toString(),
            )
            .get("count")
    }

    fun firstUnread(playerUUID: UUID, offset: Int): Mail? {
        return dataSource.database()
            .getResults(
                "SELECT * FROM `mail` WHERE `receiver_uuid` = ? AND `is_read` = false ORDER BY `created_at` ASC LIMIT ?,1",
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
                    readAt = row.get("read_at"),
                    createdAt = row.get("created_at"),
                )
            }
            .firstOrNull()
    }

    fun hasUnreadMail(playerUUID: UUID): Boolean {
        return firstUnread(playerUUID, 1) != null
    }

    fun readAll() {

    }
}