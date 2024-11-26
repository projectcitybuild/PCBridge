package com.projectcitybuild.pcbridge.paper.features.architecture.events

import com.projectcitybuild.pcbridge.paper.core.libs.store.PlayerState
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerStateUpdatedEvent(
    val prevState: PlayerState?,
    val state: PlayerState,
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
