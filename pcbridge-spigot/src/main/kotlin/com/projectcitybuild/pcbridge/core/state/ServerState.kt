package com.projectcitybuild.pcbridge.core.state

import com.projectcitybuild.pcbridge.http.responses.Badge
import com.projectcitybuild.pcbridge.support.serializable.LocalDateTimeSerializer
import com.projectcitybuild.pcbridge.support.serializable.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents in-memory, global state for the plugin.
 *
 * This state is persisted to storage on plugin shutdown,
 * and restored upon boot.
 */
@Serializable(with = UUIDSerializer::class)
data class ServerState(
    /**
     * Individual state for each online player
     */
    val players: MutableMap<UUID, PlayerState>,
    /**
     * Index of the last announcement broadcast to players on the server.
     *
     * This is used to enumerate over the announcement list, and remember
     * which one was last broadcast when reloading
     */
    val lastBroadcastIndex: Int,
) {
    companion object {
        fun default() =
            ServerState(
                players = mutableMapOf(),
                lastBroadcastIndex = -1,
            )
    }
}

@Serializable
data class PlayerState(
    @Serializable(with = LocalDateTimeSerializer::class)
    val connectedAt: LocalDateTime?,

    val badges: List<Badge>,
) {
    companion object {
        fun empty() =
            PlayerState(
                connectedAt = null,
                badges = emptyList(),
            )
    }
}
