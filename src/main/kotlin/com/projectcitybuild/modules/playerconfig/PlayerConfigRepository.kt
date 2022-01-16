package com.projectcitybuild.modules.playerconfig

import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.modules.database.DataSource
import java.util.*

class PlayerConfigRepository(
    private val cache: PlayerConfigCache,
    private val dataSource: DataSource
) {
    fun get(uuid: UUID): PlayerConfig {
        val cachedPlayer = cache.get(uuid)
        if (cachedPlayer != null) {
            return cachedPlayer
        }

        val row = dataSource.database().getFirstRow(
            "SELECT * FROM players WHERE `uuid`=(?) LIMIT 1",
            uuid.toString()
        )
        if (row != null) {
            val deserializedPlayer = PlayerConfig(
                uuid = UUID.fromString(row.get("uuid")),
                isMuted = row.get("is_muted"),
                isAllowingTPs = row.get("is_allowing_tp"),
                firstSeen = row.get("first_seen"),
            )
            cache.put(uuid, deserializedPlayer)
            return deserializedPlayer
        }

        val newCachedPlayer = PlayerConfig.default(uuid)
        save(newCachedPlayer)
        return newCachedPlayer
    }

    fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)

        dataSource.database().executeInsert(
            "INSERT INTO players VALUES (NULL, ?, ?, ?, ?)",
            player.uuid.toString(),
            player.isMuted,
            player.isAllowingTPs,
            player.firstSeen,
        )
    }
}