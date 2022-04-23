package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import java.sql.Date
import javax.inject.Inject

class ChatIgnoreRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun isIgnored(playerConfigId: Long, ignoredPlayerConfigId: Long): Boolean {
        return dataSource.database().getFirstRow(
            "SELECT * FROM chat_ignores WHERE player_id = ? AND ignored_player_id = ? LIMIT 1",
            playerConfigId,
            ignoredPlayerConfigId,
        ) != null
    }

    fun ignorerIds(playerConfigId: Long): List<Long> {
        return dataSource.database()
            .getResults("SELECT player_id FROM chat_ignores WHERE ignored_player_id = ?", playerConfigId)
            .map { (it.get("player_id") as java.math.BigInteger).toLong() }
    }

    fun add(playerConfigId: Long, ignoredPlayerConfigId: Long) {
        dataSource.database().executeInsert(
            "INSERT INTO chat_ignores VALUES (null, ?, ?, ?)",
            playerConfigId,
            ignoredPlayerConfigId,
            Date(System.currentTimeMillis()),
        )
    }

    fun delete(playerConfigId: Long, ignoredPlayerConfigId: Long) {
        dataSource.database().executeUpdate(
            "DELETE FROM chat_ignores WHERE player_id = ? AND ignored_player_id = ?",
            playerConfigId,
            ignoredPlayerConfigId,
        )
    }
}
