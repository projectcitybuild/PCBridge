package com.projectcitybuild.entities.events

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.support.spigot.eventbroadcast.BroadcastableEvent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class ConnectionPermittedEvent(
    val aggregate: Aggregate,
    val playerUUID: UUID,
) : Event(), BroadcastableEvent {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}
