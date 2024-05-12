package com.projectcitybuild.pcbridge.features.bans.events

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class ConnectionPermittedEvent(
    val aggregate: Aggregate,
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
