package com.projectcitybuild.pcbridge.paper.features.groups.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerSyncRequestedEvent(
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