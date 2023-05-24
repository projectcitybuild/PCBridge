package com.projectcitybuild.repositories

import com.projectcitybuild.core.database.DataSource
import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import java.time.LocalDateTime
import java.util.UUID

class PlayerConfigRepository(
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
                isChatBadgeDisabled = row.get("is_badge_disabled"),
                firstSeen = row.get("first_seen"),
            )
            cache.put(uuid, deserializedPlayer)
            return deserializedPlayer
        }

        return null
    }

    fun add(
        uuid: UUID,
        isMuted: Boolean,
        isChatBadgeDisabled: Boolean,
        firstSeen: LocalDateTime,
    ): PlayerConfig {
        val lastInsertedId = dataSource.database().executeInsert(
            "INSERT INTO players VALUES (NULL, ?, ?, ?, ?)",
            uuid.toString(),
            isMuted,
            isChatBadgeDisabled,
            firstSeen,
        )
        val playerConfig = PlayerConfig(
            id = lastInsertedId,
            uuid = uuid,
            isMuted = isMuted,
            isChatBadgeDisabled = isChatBadgeDisabled,
            firstSeen = firstSeen,
        )
        cache.put(uuid, playerConfig)

        return playerConfig
    }

    fun save(player: PlayerConfig) {
        cache.put(player.uuid, player)

        dataSource.database().executeUpdate(
            "UPDATE players SET `uuid` = ?, `is_muted` = ?, `is_badge_disabled` = ?, `first_seen` = ? WHERE `id`= ?",
            player.uuid.toString(),
            player.isMuted,
            player.isChatBadgeDisabled,
            player.firstSeen,
            player.id,
        )
    }
}
