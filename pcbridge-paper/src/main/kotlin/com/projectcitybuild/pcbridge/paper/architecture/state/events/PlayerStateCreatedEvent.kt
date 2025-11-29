package com.projectcitybuild.pcbridge.paper.architecture.state.events

import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSession
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/**
 * Represents that PlayerState was created for a player.
 *
 * Note: This is fired immediately after connecting, before a Player instance
 * actually exists
 */
class PlayerStateCreatedEvent(
    val state: PlayerSession,
    val playerUUID: UUID,
) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
