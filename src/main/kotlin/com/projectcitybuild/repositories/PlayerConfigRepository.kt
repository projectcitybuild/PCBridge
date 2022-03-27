package com.projectcitybuild.repositories

import com.projectcitybuild.core.infrastructure.database.DataSource
import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.integrations.internal.playercache.PlayerConfigCache
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class PlayerConfigRepository @Inject constructor(
    private val cache: PlayerConfigCache,
    private val dataSource: DataSource
) {
    fun get(uuid: UUID): PlayerConfig? {
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
                id = row.get("id"),
                uuid = UUID.fromString(row.get("uuid")),
                isMuted = row.get("is_muted"),
                isAllowingTPs = row.get("is_allowing_tp"),
                firstSeen = row.get("first_seen"),
            )
            cache.put(uuid, deserializedPlayer)
            return deserializedPlayer
        }

        return null
    }

    fun add(uuid: UUID, isMuted: Boolean, isAllowingTPs: Boolean, firstSeen: LocalDateTime): PlayerConfig {
        val lastInsertedId = dataSource.database().executeInsert(
            "INSERT INTO players VALUES (NULL, ?, ?, ?, ?)",
            uuid.toString(),
            isMuted,
            isAllowingTPs,
            firstSeen,
        )
        val playerConfig = PlayerConfig(
            id = lastInsertedId,
            uuid,
            isMuted,
            isAllowingTPs,
            firstSeen,
        )
        cache.put(uuid, playerConfig)

        return playerConfig
    }

    fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)

        dataSource.database().executeUpdate(
            "UPDATE players SET `uuid` = ?, `is_muted` = ?, `is_allowing_tp` = ?, `first_seen` = ? WHERE `id`= ?",
            player.uuid.toString(),
            player.isMuted,
            player.isAllowingTPs,
            player.firstSeen,
            player.id,
        )
    }
}
