package com.projectcitybuild.pcbridge.paper.architecture.state.events

import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerStateDestroyedEvent(
    val playerData: PlayerState?,
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
