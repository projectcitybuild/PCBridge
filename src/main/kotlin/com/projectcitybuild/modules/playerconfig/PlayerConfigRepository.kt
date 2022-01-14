package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.modules.database.DataSource
import java.util.*

class PlayerConfigRepository(
    private val cache: PlayerConfigCache,
    private val dataSource: DataSource
) {
    suspend fun get(uuid: UUID): PlayerConfig {
        val cachedPlayer = cache.get(uuid)
        if (cachedPlayer != null) {
            return cachedPlayer
        }

        val statement = dataSource.connection().prepareStatement(
            "SELECT * FROM players WHERE `uuid`=(?)"
        ).apply {
            setString(1, uuid.toString())
        }
        val resultSet = statement.executeQuery()
        var serializedPlayer: PlayerConfig? = null
        if (resultSet.next()) {
            serializedPlayer = PlayerConfig(
                uuid = UUID.fromString(resultSet.getString(2)),
                isMuted = resultSet.getBoolean(3),
                isAllowingTPs = resultSet.getBoolean(4),
                chatIgnoreList = mutableSetOf(), // TODO,
                firstSeen = resultSet.getDate(5)
            )
            cache.put(uuid, serializedPlayer)
        }
        resultSet.close()

        if (serializedPlayer != null) {
            return serializedPlayer
        }

        val newCachedPlayer = PlayerConfig.default(uuid)
        save(newCachedPlayer)
        return newCachedPlayer
    }

    suspend fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)

        val statement = dataSource.connection().prepareStatement(
            "INSERT INTO players VALUES (NULL, ?, ?, ?, ?)"
        ).apply {
            setString(1, player.uuid.toString())
            setBoolean(2, player.isMuted)
            setBoolean(3, player.isAllowingTPs)
            setDate(4, player.firstSeen)
        }
        statement.executeUpdate()
    }
}