package com.projectcitybuild.core.state

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents in-memory, global state for the plugin.
 *
 * This state is persisted to storage on plugin shutdown, and
 * rehydrated upon boot.
 */
@Serializable
data class ServerState(
    val players: MutableMap<UUID, PlayerState>
)

@Serializable
data class PlayerState(
    val connectedAt: LocalDateTime,
)
