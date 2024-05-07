package com.projectcitybuild.features.joinmessages.repositories

import com.projectcitybuild.core.database.DatabaseSession
import com.projectcitybuild.features.joinmessages.PlayerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Date
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

class PlayerConfigRepository(
    private val dataSource: DatabaseSession
) {
    suspend fun get(
        uuid: UUID,
    ): PlayerConfig? = withContext(Dispatchers.IO) {
        dataSource.connect { connection ->
            connection.prepareStatement("SELECT * FROM players WHERE `uuid`=(?) LIMIT 1")
                .apply { setString(1, uuid.toString()) }
                .use { it.executeQuery() }
                .use { it.firstRow() }
                ?.toPlayerConfig()
        }
    }

    suspend fun add(
        uuid: UUID,
        firstSeen: LocalDateTime,
    ): PlayerConfig = withContext(Dispatchers.IO) {
        val lastInsertedId = dataSource.connect { connection ->
            connection.prepareStatement("INSERT INTO players VALUES (NULL, ?, ?, ?, ?)")
                .apply {
                    setString(1, uuid.toString())
                    setBoolean(2, false)
                    setBoolean(3, false)
                    setDate(4, Date.valueOf(firstSeen.toLocalDate()))
                }
                .use { it.executeUpdate() }
        }
        PlayerConfig(
            id = lastInsertedId.toLong(),
            uuid = uuid,
            firstSeen = firstSeen,
        )
    }

    suspend fun save(
        player: PlayerConfig,
    ) = withContext(Dispatchers.IO) {
        dataSource.connect { connection ->
            connection.prepareStatement("UPDATE players SET `uuid` = ?, `first_seen` = ? WHERE `id`= ?")
                .apply {
                    setString(1, player.uuid.toString())
                    setDate(4, Date.valueOf(player.firstSeen.toLocalDate()))
                    setLong(5, player.id)
                }
                .use { it.executeUpdate() }
        }
    }
}

private fun ResultSet.firstRow(): ResultSet? {
    if (next()) return this
    return null
}

private fun ResultSet.toPlayerConfig() = PlayerConfig(
    id = getLong("id"),
    uuid = UUID.fromString(getString("uuid")),
    firstSeen = getTimestamp("first_seen").toLocalDateTime(),
)