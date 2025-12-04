package com.projectcitybuild.pcbridge.paper.architecture.state.data

import com.projectcitybuild.pcbridge.http.shared.serialization.serializable.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents in-memory, global state for the plugin.
 *
 * This state is persisted to storage on plugin shutdown
 * and restored upon boot.
 */
@Serializable(with = UUIDSerializer::class)
data class ServerState(

    /**
     * Index of the last announcement broadcast to players on the server.
     *
     * This is used to enumerate over the announcement list, and remember
     * which one was last broadcast when reloading
     */
    val lastBroadcastIndex: Int = 0,

    /**
     * Whether the server is in maintenance mode or not
     */
    val maintenance: Boolean = false,
)
